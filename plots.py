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

# load data given the following parameters
def load_data(dataset, k):

    BCLP_update_times = load_file('results/' + dataset + '_BCLP_updatetime_' + str(k), nano_to_seconds)
    HK20_update_times = load_file('results/' + dataset + '_HK20_updatetime_' + str(k), nano_to_seconds)

    BCLP_query_times = load_file('results/' + dataset + '_BCLP_querytime_' + str(k), nano_to_seconds)
    HK20_query_times = load_file('results/' + dataset + '_HK20_querytime_' + str(k), nano_to_seconds)

    BCLP_cost = load_file('results/' + dataset + '_BCLP_cost_' + str(k), float)
    HK20_cost = load_file('results/' + dataset + '_HK20_cost_' + str(k), float)
    # MP03_cost = load_file('results/' + dataset + '_MP03_cost_' + str(k), float)
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

    plt.suptitle(str(n) + ' Updates from the ' + dataset + ' Dataset, with k = ' + str(k))

    fig.tight_layout()

    plt.show()

if __name__ == '__main__':

    dataset, k = sys.argv[1], int(sys.argv[2])

    plot_data(dataset, k)


# plot_data('song', 50)

# load in the files
# BCLP_update_times_50 = load_file("../results/BCLP_updatetime_50", nano_to_seconds)
# HK20_update_times_50 = load_file("../results/HK20_updatetime_50", nano_to_seconds)
#
# BCLP_query_times_50 = load_file("../results/BCLP_querytime_50", nano_to_seconds)
# HK20_query_times_50 = load_file("../results/HK20_querytime_50", nano_to_seconds)
#
# BCLP_cost_50 = load_file("../results/BCLP_cost_50", float)
# HK20_cost_50 = load_file("../results/HK20_cost_50", float)
# MP03_cost_50 = load_file("../results/MP03_cost_50", float)
# kmeanspp_cost_50 = load_file("../results/kmeanspp_cost_50", float)

# # number of updates
# n = len(BCLP_update_times_50)
#
# # query frequency
# q = len(BCLP_query_times_50)
#
# x_updates = np.linspace(1, n, n)
# x_queries = np.linspace(1, n, q)

# initialise the arrays to store the values
# BCLP_update_times_10 = [0]*n
# # BCLP_update_times_50 = [0]*n
# BCLP_update_times_250 = [0]*n
#
# HK20_update_times_10 = [0]*n
# # HK20_update_times_50 = [0]*n
# HK20_update_times_250 = [0]*n
#
# BCLP_query_times_10 = [0]*q
# # BCLP_query_times_50 = [0]*q
# BCLP_query_times_250 = [0]*q
#
# HK20_query_times_10 = [0]*q
# # HK20_query_times_50 = [0]*q
# HK20_query_times_250 = [0]*q
#
# BCLP_cost_10 = [0]*q
# # BCLP_cost_50 = [0]*q
# BCLP_cost_250 = [0]*q
#
# HK20_cost_10 = [0]*q
# # HK20_cost_50 = [0]*q
# HK20_cost_250 = [0]*q

# create the plots
# fig, axs = plt.subplots(3, 3)

# update times
# axs[0, 0].plot(x_updates, BCLP_update_times_10, 'b-', label='BCLP')
# axs[0, 0].plot(x_updates, HK20_update_times_10, 'r-', label='HK20')
# axs[0, 0].set_title('Total Update Time, k = 10')
# axs[0, 0].set(xlabel='Updates', ylabel='Total Update Time (sec)')
# axs[0, 0].legend();
#
# axs[0, 1].plot(x_updates, BCLP_update_times_50, 'b-', label='BCLP')
# axs[0, 1].plot(x_updates, HK20_update_times_50, 'r-', label='HK20')
# axs[0, 1].set_title('Total Update Time, k = 50')
# axs[0, 1].set(xlabel='Updates', ylabel='Total Update Time (sec)')
# axs[0, 1].legend();
#
# axs[0, 2].plot(x_updates, BCLP_update_times_250, 'b-', label='BCLP')
# axs[0, 2].plot(x_updates, HK20_update_times_250, 'r-', label='HK20')
# axs[0, 2].set_title('Total Update Time, k = 250')
# axs[0, 2].set(xlabel='Updates', ylabel='Total Update Time (sec)')
# axs[0, 2].legend();

# query times
# axs[1, 0].plot(x_queries, BCLP_query_times_10, 'b-', label='BCLP')
# axs[1, 0].plot(x_queries, HK20_query_times_10, 'r-', label='HK20')
# axs[1, 0].set_title('Total Query Time, k = 10')
# axs[1, 0].set(xlabel='Updates', ylabel='Total Query Time (sec)')
# axs[1, 0].legend();
#
# axs[1, 1].plot(x_queries, BCLP_query_times_50, 'b-', label='BCLP')
# axs[1, 1].plot(x_queries, HK20_query_times_50, 'r-', label='HK20')
# axs[1, 1].set_title('Total Query Time, k = 50')
# axs[1, 1].set(xlabel='Updates', ylabel='Total Query Time (sec)')
# axs[1, 1].legend();
#
# axs[1, 2].plot(x_queries, BCLP_query_times_250, 'b-', label='BCLP')
# axs[1, 2].plot(x_queries, HK20_query_times_250, 'r-', label='HK20')
# axs[1, 2].set_title('Total Query Time, k = 250')
# axs[1, 2].set(xlabel='Updates', ylabel='Total Query Time (sec)')
# axs[1, 2].legend();

# costs
# axs[2, 0].plot(x_queries, BCLP_cost_10, 'b-', label='BCLP')
# axs[2, 0].plot(x_queries, HK20_cost_10, 'r-', label='HK20')
# axs[2, 0].set_title('Cost of Solution, k = 10')
# axs[2, 0].set(xlabel='Updates', ylabel='Cost')
# axs[2, 0].legend();
#
# axs[2, 1].plot(x_queries, BCLP_cost_50, 'b-', label='BCLP')
# axs[2, 1].plot(x_queries, HK20_cost_50, 'r-', label='HK20')
# axs[2, 1].set_title('Cost of Solution, k = 50')
# axs[2, 1].set(xlabel='Updates', ylabel='Cost')
# axs[2, 1].legend();
#
# axs[2, 2].plot(x_queries, BCLP_cost_250, 'b-', label='BCLP')
# axs[2, 2].plot(x_queries, HK20_cost_250, 'r-', label='HK20')
# axs[2, 2].set_title('Cost of Solution, k = 250')
# axs[2, 2].set(xlabel='Updates', ylabel='Cost')
# axs[2, 2].legend();

##############################

# # create the plots
# fig, axs = plt.subplots(1, 3)
#
# # update times
# axs[0].plot(x_updates, HK20_update_times_50, 'r-', label='HK20')
# axs[0].plot(x_updates, BCLP_update_times_50, 'b-', label='BCLP')
# axs[0].set_title('Total Update Time, k = 50')
# axs[0].set(xlabel='Updates', ylabel='Total Update Time (sec)')
# axs[0].legend();
#
# axs[1].plot(x_queries, HK20_query_times_50, 'r-', label='HK20')
# axs[1].plot(x_queries, BCLP_query_times_50, 'b-', label='BCLP')
# axs[1].set_title('Total Query Time, k = 50')
# axs[1].set(xlabel='Updates', ylabel='Total Query Time (sec)')
# axs[1].legend();
#
# axs[2].plot(x_queries, HK20_cost_50, 'r-', label='HK20')
# axs[2].plot(x_queries, BCLP_cost_50, 'b-', label='BCLP')
# axs[2].plot(x_queries, MP03_cost_50, 'g-', label='MP03')
# axs[2].plot(x_queries, kmeanspp_cost_50, 'c-', label='kmeans++')
# axs[2].set_title('Cost of Solution, k = 50')
# axs[2].set(xlabel='Updates', ylabel='Cost')
# axs[2].legend();
#
# plt.show()
