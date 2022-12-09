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

# create a plot for dataset and k
def plot_data(rows):

    fig, axs = plt.subplots(len(rows), 3, figsize=(15, 5*len(rows)))

    for i in range(len(rows)):
        plot_data_row(axs[i], rows[i][0], rows[i][1], rows[i][2], rows[i][3], rows[i][4])

    fig.tight_layout()

    plt.savefig('figures/test_results.pdf', format='pdf', bbox_inches='tight')

    plt.show()

# create a plot for dataset and k
def plot_data_row(axs, dataset, k, params, algo, colors):

    data = []

    for p in params:
        data = data + [load_data_single(dataset, k, p, algo)]

    n = len(data[0][0])
    q = len(data[0][1])

    x_updates = np.linspace(1, n, n)
    x_queries = np.linspace(1, n, q)

    # update times
    for i in range(len(params)):
        axs[0].plot(x_updates, data[i][0], label=str(params[i]) + '_' + algo, color=colors[i])
    axs[0].set_title('Total Update Time')
    axs[0].set(xlabel='Updates', ylabel='Total Update Time (sec)')
    axs[0].legend()

    # query times
    for i in range(len(params)):
        axs[1].plot(x_queries, data[i][1], label=str(params[i]) + '_' + algo, color=colors[i])
    axs[1].set_title('Total Query Time')
    axs[1].set(xlabel='Updates', ylabel='Total Query Time (sec)')
    axs[1].legend()

    # costs
    for i in range(len(params)):
        axs[2].plot(x_queries, data[i][2], label=str(params[i]) + '_' + algo, color=colors[i])
    axs[2].set_title('Cost of Solution')
    axs[2].set(xlabel='Updates', ylabel='Cost')
    axs[2].legend()

    plt.suptitle('dataset = ' + dataset + ', k = ' + str(k) + ', ' + str(n) + ' updates, ' + str(q) + ' queries')

if __name__ == '__main__':

    blues = ['#20B2AA', '#00BFFF', '#0000FF']
    reds = ['#FFA500', '#EE82EE', '#FF0000']

    plot_data([['census', 10, [15, 30, 60], 'BCLP', blues], ['census', 10, [250, 500, 1000], 'HK20', reds]])
