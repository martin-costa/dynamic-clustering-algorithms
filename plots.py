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

def load_data_single(dataset, k, param, algo):

    update_times = load_file('results/' + dataset + '_' + str(k) + '_' + str(param) + '_' + algo + '_updatetime', nano_to_seconds)
    query_times = load_file('results/' + dataset + '_' + str(k) + '_' + str(param) + '_' + algo + '_querytime', nano_to_seconds)
    query_times = amortize(query_times)
    costs = load_file('results/' + dataset + '_' + str(k) + '_' + str(param) + '_' + algo + '_cost', float)

    return update_times, query_times, costs

# create a plot for dataset and k
def plot_data(pages):

    # stores the figures for each page
    figs = []

    for page in pages:
        figs = figs + [plot_data_page(page)]

    with PdfPages('test_results.pdf') as pdf:

        for fig in figs:
            pdf.savefig(fig)  # saves the current figure into a pdf page
            plt.close()

    plt.show()

# create a plot for dataset and k
def plot_data_page(page):

    fig, axs = plt.subplots(len(page), 3, figsize=(18, 5*len(page)))

    for i in range(len(page)):
        plot_data_row(axs[i], page[i][0], page[i][1], page[i][2], page[i][3], page[i][4])

    fig.tight_layout()

    return fig

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
    axs[0].set_title('Total Update Time (' + dataset + ', k = ' + str(k) + ')')
    axs[0].set(xlabel='Updates', ylabel='Total Update Time (sec)')
    axs[0].legend()

    # query times
    for i in range(len(params)):
        axs[1].plot(x_queries, data[i][1], label=str(params[i]) + '_' + algo, color=colors[i])
    axs[1].set_title('Average Query Time (' + dataset + ', k = ' + str(k) + ')')
    axs[1].set(xlabel='Updates', ylabel='Average Query Time (sec)')
    axs[1].legend()

    # costs
    for i in range(len(params)):
        axs[2].plot(x_queries, data[i][2], label=str(params[i]) + '_' + algo, color=colors[i])
    axs[2].set_title('Cost of Solution (' + dataset + ', k = ' + str(k) + ')')
    axs[2].set(xlabel='Updates', ylabel='Cost')
    axs[2].legend()

    # plt.suptitle('dataset = ' + dataset + ', k = ' + str(k) + ', ' + str(n) + ' updates, ' + str(q) + ' queries')

# construct a page for some value of k
def get_page(k, alphas, thresholds):

    blues = ['#20B2AA', '#00BFFF', '#0000FF']
    reds = ['#FFA500', '#EE82EE', '#FF0000']

    page = [

     ['census', k, alphas, 'BCLP', blues],
     ['census', k, thresholds, 'HK20', reds],
     ['song', k, alphas, 'BCLP', blues],
     ['song', k, thresholds, 'HK20', reds],
     ['kddcup', k, alphas, 'BCLP', blues],
     ['kddcup', k, thresholds, 'HK20', reds]

    ]

    return page

def get_pages(kValues, alphas, thresholds):

    pages = []
    for k in kValues:
        pages = pages + [get_page(k, alphas, thresholds)]

    return pages

if __name__ == '__main__':

    alphas = [250, 500, 1000]
    thresholds = [250, 500, 1000]

    plot_data(get_pages([10, 50, 100], alphas, thresholds))
