import math
import numpy as np

# implementation of dynamic version of Mettu-Plaxton's static k-medians algorithm

# our dynamic coreset algoithm, based on a dynamization of MP
class DynamicMP:

    # initialize the data structure
    def __init__(self, d, k, alpha, beta):

        # the metric
        self.d = d

        # parameters
        self.k = k
        self.alpha = alpha
        self.beta = beta

        # sampling paramter
        self.sampleSize = floor(self.alpha * self.k)

    # class to define a layer of the dynamic MP algorithm
    class Layer:

        # construct layer
        def __init__(self, U):

            # the metric space to be clustered
            self.U = U

            # the size of the metrix space
            self.n = len(self.U)

            # sample floor(\alpha * k) elements from U u.a.r with replacement
            samples = np.random.randint(self.sampleSize, size=self.n)

        # insert a point into the layer
        def insert(x):

            return None

        # delete a point in the layer
        def delete(x):

            return None

    # insert a point into the data structure
    def insert(x):

        return None

    # delete a point from the data structure
    def delete(x):

        return None

print(115)
