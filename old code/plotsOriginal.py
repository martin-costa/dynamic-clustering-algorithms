import matplotlib.pyplot as plt
import numpy as np
import sys

# loads a long from nanoseconds as a float in seconds
def nano_to_seconds(n):
    return int(n)*0.000000001

# function to load file at path
def load_file(path, f):

    # open the file
    file = open(path)
    strings = file.read().replace('\x00', '').split("#")
    file.close()

    values = []
    for s in strings:
        if len(s) > 0:
            values.append(f(s))

    return values

def load_data_single(dataset, k, param, algo):

    update_times = load_file('results/' + dataset + '_' + str(k) + '_' + str(param) + '_' + algo + '_updatetime', nano_to_seconds)
    query_times = load_file('results/' + dataset + '_' + str(k) + '_' + str(param) + '_' + algo + '_querytime', nano_to_seconds)
    costs = load_file('results/' + dataset + '_' + str(k) + '_' + str(param) + '_' + algo + '_cost', float)

    return update_times, query_times, costs

# load data given the following parameters
def load_data(dataset, k):

    BCLP_update_times = load_file('results/' + dataset + '_BCLP_updatetime_' + str(k), nano_to_seconds)
    HK20_update_times = load_file('results/' + dataset + '_HK20_updatetime_' + str(k), nano_to_seconds)

    BCLP_query_times = load_file('results/' + dataset + '_BCLP_querytime_' + str(k), nano_to_seconds)
    HK20_query_times = load_file('results/' + dataset + '_HK20_querytime_' + str(k), nano_to_seconds)

    BCLP_cost = load_file('results/' + dataset + '_BCLP_cost_' + str(k), float)
    HK20_cost = load_file('results/' + dataset + '_HK20_cost_' + str(k), float)
    kmeanspp_cost = load_file('results/' + dataset + '_kmeanspp_cost_' + str(k), float)

    # make sure data has the right dimensions
    BCLP_update_times, HK20_update_times = slice_data([BCLP_update_times, HK20_update_times])
    BCLP_query_times, HK20_query_times, BCLP_cost, HK20_cost, kmeanspp_cost = slice_data([BCLP_query_times, HK20_query_times, BCLP_cost, HK20_cost, kmeanspp_cost])

    n = len(BCLP_update_times)
    q = len(BCLP_query_times)

    return n, q, [BCLP_update_times, HK20_update_times], [BCLP_query_times, HK20_query_times], [BCLP_cost, HK20_cost, kmeanspp_cost]

# ensure all arrays are the same length
def slice_data(arrays):

    # smallest array length
    l = len(arrays[0])
    for i in range(1, len(arrays)):
        l = min(l, len(arrays[i]))

    for i in range(len(arrays)):
        arrays[i] = arrays[i][0:l]

    return arrays

# create a plot for dataset and k
def plot_data(dataset, k):

    data = load_data(dataset, k)

    n = data[0]
    q = data[1]

    x_updates = np.linspace(1, n, n)
    x_queries = np.linspace(1, n, q)

    fig, axs = plt.subplots(1, 3, figsize=(12, 5))

    # update times
    axs[0].plot(x_updates, data[2][1], 'r-', label='HK20')
    axs[0].plot(x_updates, data[2][0], 'b-', label='BCLP')
    axs[0].set_title('Total Update Time')
    axs[0].set(xlabel='Updates', ylabel='Total Update Time (sec)')
    axs[0].legend();

    # query times
    axs[1].plot(x_queries, data[3][1], 'r-', label='HK20')
    axs[1].plot(x_queries, data[3][0], 'b-', label='BCLP')
    axs[1].set_title('Total Query Time')
    axs[1].set(xlabel='Updates', ylabel='Total Query Time (sec)')
    axs[1].legend();

    # costs
    axs[2].plot(x_queries, data[4][2], 'c-', label='kmeans++')
    axs[2].plot(x_queries, data[4][1], 'r-', label='HK20')
    axs[2].plot(x_queries, data[4][0], 'b-', label='BCLP')
    axs[2].set_title('Cost of Solution')
    axs[2].set(xlabel='Updates', ylabel='Cost')
    axs[2].legend();

    plt.suptitle(str(n) + ' updates from the ' + dataset + ' dataset, ' + str(q) + ' queries, and k = ' + str(k))

    fig.tight_layout()

    plt.show()

# create a plot for dataset and k
def plot_data_row(dataset, k, params, algo):

    data = []

    for p in params:
        data = data + [load_data_single(dataset, k, p, algo)]

    n = len(data[0][0])
    q = len(data[0][1])

    x_updates = np.linspace(1, n, n)
    x_queries = np.linspace(1, n, q)

    fig, axs = plt.subplots(1, 3, figsize=(12, 5))

    colors = ['#20B2AA', '#00BFFF', '#0000FF']

    # update times
    for i in range(len(params)):
        axs[0].plot(x_updates, data[i][0], label=str(params[i]) + '_' + algo, color=colors[i])
    axs[0].set_title('Total Update Time')
    axs[0].set(xlabel='Updates', ylabel='Total Update Time (sec)')
    axs[0].legend();

    # query times
    for i in range(len(params)):
        axs[1].plot(x_queries, data[i][1], label=str(params[i]) + '_' + algo, color=colors[i])
    axs[1].set_title('Total Query Time')
    axs[1].set(xlabel='Updates', ylabel='Total Query Time (sec)')
    axs[1].legend();

    # costs
    for i in range(len(params)):
        axs[2].plot(x_queries, data[i][2], label=str(params[i]) + '_' + algo, color=colors[i])
    axs[2].set_title('Cost of Solution')
    axs[2].set(xlabel='Updates', ylabel='Cost')
    axs[2].legend();

    plt.suptitle(str(n) + ' updates from the ' + dataset + ' dataset, ' + str(q) + ' queries, and k = ' + str(k))

    fig.tight_layout()

    plt.savefig('figures/test_results.pdf', format='pdf', bbox_inches='tight')

    plt.show()

# # generate a figure with many tests and export to pdf
# def export_tests():

if __name__ == '__main__':

    # dataset, k = sys.argv[1], int(sys.argv[2])
    #
    # plot_data(dataset, k)

    plot_data_params('census', 10, [15, 30, 60], 'BCLP')
