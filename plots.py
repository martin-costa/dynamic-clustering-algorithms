import matplotlib.pyplot as plt
import numpy as np

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

# load in the files
BCLP_update_times_50 = load_file("../results/BCLP_updatetime_50", nano_to_seconds)
HK20_update_times_50 = load_file("../results/HK20_updatetime_50", nano_to_seconds)

BCLP_query_times_50 = load_file("../results/BCLP_querytime_50", nano_to_seconds)
HK20_query_times_50 = load_file("../results/HK20_querytime_50", nano_to_seconds)

BCLP_cost_50 = load_file("../results/BCLP_cost_50", float)
HK20_cost_50 = load_file("../results/HK20_cost_50", float)

# number of updates
n = len(BCLP_update_times_50)

# query frequency
q = len(BCLP_query_times_50)

x_updates = np.linspace(1, n, n)
x_queries = np.linspace(1, n, q)

# initialise the arrays to store the values
BCLP_update_times_10 = [0]*n
# BCLP_update_times_50 = [0]*n
BCLP_update_times_250 = [0]*n

HK20_update_times_10 = [0]*n
# HK20_update_times_50 = [0]*n
HK20_update_times_250 = [0]*n

BCLP_query_times_10 = [0]*q
# BCLP_query_times_50 = [0]*q
BCLP_query_times_250 = [0]*q

HK20_query_times_10 = [0]*q
# HK20_query_times_50 = [0]*q
HK20_query_times_250 = [0]*q

BCLP_cost_10 = [0]*q
# BCLP_cost_50 = [0]*q
BCLP_cost_250 = [0]*q

HK20_cost_10 = [0]*q
# HK20_cost_50 = [0]*q
HK20_cost_250 = [0]*q

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

# create the plots
fig, axs = plt.subplots(1, 3)

# update times
axs[0].plot(x_updates, HK20_update_times_50, 'r-', label='HK20')
axs[0].plot(x_updates, BCLP_update_times_50, 'b-', label='BCLP')
axs[0].set_title('Total Update Time, k = 50')
axs[0].set(xlabel='Updates', ylabel='Total Update Time (sec)')
axs[0].legend();

axs[1].plot(x_queries, HK20_query_times_50, 'r-', label='HK20')
axs[1].plot(x_queries, BCLP_query_times_50, 'b-', label='BCLP')
axs[1].set_title('Total Query Time, k = 50')
axs[1].set(xlabel='Updates', ylabel='Total Query Time (sec)')
axs[1].legend();

axs[2].plot(x_queries, HK20_cost_50, 'r-', label='HK20')
axs[2].plot(x_queries, BCLP_cost_50, 'b-', label='BCLP')
axs[2].set_title('Cost of Solution, k = 50')
axs[2].set(xlabel='Updates', ylabel='Cost')
axs[2].legend();

plt.show()
