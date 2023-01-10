import matplotlib.pyplot as plt
import numpy as np
from matplotlib.backends.backend_pdf import PdfPages
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

# method to turn total query time to average
def amortize(values):

    for i in range(len(values)):
        values[i] = values[i]/(i + 1)

    return values

def load_data_single(dataset, k, algo, dir='results/'):

    update_times = load_file(dir + dataset + '_' + str(k) + '_' + algo + '_updatetime', nano_to_seconds)
    query_times = load_file(dir + dataset + '_' + str(k) + '_' + algo + '_querytime', nano_to_seconds)
    query_times = amortize(query_times)
    costs = load_file(dir + dataset + '_' + str(k) + '_' + algo + '_cost', float)

    return update_times, query_times, costs

# create a plot for dataset and k
def plot_data(pages, dir='results/'):

    # stores the figures for each page
    figs = []

    for page in pages:
        figs = figs + [plot_data_page(page, dir)]

    with PdfPages('test_results.pdf') as pdf:

        for fig in figs:
            pdf.savefig(fig)  # saves the current figure into a pdf page
            plt.close()

    plt.show()

# create a plot for dataset and k
def plot_data_page(page, dir='results/'):

    fig, axs = plt.subplots(len(page), 3*len(page[0][2]), figsize=(6*3*len(page[0][2]), 5*len(page)))

    if len(page) == 1:
        plot_data_row_2(axs, page[0][0], page[0][1], page[0][2], page[0][3], dir)
    else:
        for i in range(len(page)):
            plot_data_row_2(axs[i], page[i][0], page[i][1], page[i][2], page[i][3], dir)

    fig.tight_layout()

    return fig

# create a plot for dataset and k
def plot_data_row_2(axs, dataset, k, algos, colors, dir='results/'):

    data = [[]]*len(algos)

    for i in range(len(algos)):
        for alg in algos[i]:
            data[i] = data[i] + [load_data_single(dataset, k, alg, dir)]

    n = len(data[0][0][0])
    q = len(data[0][0][1])

    x_updates = np.linspace(1, n, n)
    x_queries = np.linspace(1, n, q)

    for j in range(len(algos)):

        # update times
        for i in range(len(algos[j])):
            axs[j].plot(x_updates, data[j][i][0], label=algos[j][i], color=colors[j][i])
        axs[j].set_title('Total Update Time (' + dataset + ', k = ' + str(k) + ')')
        axs[j].set(xlabel='Updates', ylabel='Total Update Time (sec)')
        axs[j].set_yscale('log')
        axs[j].legend()

        # costs
        for i in range(len(algos[j])):
            axs[len(algos)+j].plot(x_queries, data[j][i][2], label=algos[j][i], color=colors[j][i])
        axs[len(algos)+j].set_title('Cost of Solution (' + dataset + ', k = ' + str(k) + ')')
        axs[len(algos)+j].set(xlabel='Updates', ylabel='Cost')
        axs[len(algos)+j].legend()

        # query times
        for i in range(len(algos[j])):
            axs[2*len(algos)+j].plot(x_queries, data[j][i][1], label=algos[j][i], color=colors[j][i])
        axs[2*len(algos)+j].set_title('Average Query Time (' + dataset + ', k = ' + str(k) + ')')
        axs[2*len(algos)+j].set(xlabel='Updates', ylabel='Average Query Time (sec)')
        axs[2*len(algos)+j].legend()

# construct a page for some value of k
def get_page(k, algos):

    blues = ['#20B2AA', '#00BFFF', '#0000FF']
    reds = ['#FFA500', '#EE82EE', '#FF0000']

    q = len(algos)

    page = [

     # ['census', k, algos, [blues + reds]],
     # ['song', k, algos, [blues + reds]],
     # ['kddcup', k, algos, [blues + reds]],



     # ['census', k, algos, [['#0000FF'] + ['#FF0000']]],
     # ['song', k, algos, [['#0000FF'] + ['#FF0000']]],
     ['kddcup', k, algos, [['#0000FF'] + ['#FF0000']]],


    ]

    return page

def get_pages(kValues, algos):

    pages = []
    for k in kValues:
        pages = pages + [get_page(k, algos)]

    return pages

if __name__ == '__main__':

    # print justification

    # bclp_algos = ['250_BCLP', '500_BCLP', '1000_BCLP']
    # hk20_algos = ['250_HK20', '500_HK20', '1000_HK20']
    #
    # plot_data(get_pages([50], [bclp_algos + hk20_algos]), 'results_justification_random/')

    # print main results

    # plot_data(get_pages([10, 50, 100], [['500_BCLP'] + ['1000_HK20']]), 'results/')

    plot_data(get_pages([50], [['500_BCLP'] + ['1000_HK20']]), 'results/')
