import java.util.*;
import java.io.*;

/*

implementation of (static) local search of Arya et al.

*/

public class LocalSearch {

  // parameter for k clustering
  private int k;

  // metric used to evaluate distances
  private Metric metric;

  // controls approximation ratio
  private float epsilon;

  // define the parameters
  public LocalSearch(int k, Metric metric, float epsilon) {
    this.k = k;
    this.metric = metric;
    this.epsilon = epsilon;
  }

  // given a weighted metric space, find a k clustering of the points
  public TreeMap<Integer, Integer> cluster(TreeMap<Integer, float[]> points, TreeMap<Integer, Float> weights) {

    // the solution to be constructed
    TreeMap<Integer, Integer> solution = new TreeMap<Integer, Integer>();

    // collection of points to search for swaps
    TreeMap<Integer, Integer> swapCandidates = new TreeMap<Integer, Integer>();

    // create an iterator of the points
    Iterator<Integer> pointIterator = points.iterator();

    // place first k points into solution
    for (int i = 0; i < this.k; i++) {
      if (!pointIterator.hasNext())
        return solution;
      int x = pointIterator.next();
      solution.put(x,x);
    }

    // if we have <= k points in the space
    if (points.size() <= this.k) {
      return solution;
    }

    // place the remaining points into a BBT
    while (pointIterator.hasNext()) {
      int x = pointIterator.next();
      swapCandidates.put(x,x);
    }

    // perform local modifications until we reach epsilon stability
    boolean unstable = true;
    while (unstable) {
      unstable = localModification(solution, swapCandidates, points, weights);
    }

    return solution;
  }

  // perform a step of local modification and return if we performed a swap
  private boolean localModification(TreeMap<Integer, Integer> solution,
                                    TreeMap<Integer, Integer> swapCandidates,
                                    TreeMap<Integer, float[]> points,
                                    TreeMap<Integer, Float> weights) {

    // check if we can swap some pair of points to get a better solution

    // create an iterator for swap candidates
    Iterator<Integer> swapCandidateIterator = swapCandidates.iterator();

    while (swapCandidateIterator.hasNext()) {

      int x = swapCandidateIterator.next();

      // create an iterator for points in solution
      Iterator<Integer> solutionIterator = solution.iterator();

      while (solutionIterator.hasNext()) {
        int y = solutionIterator.next();

        // if solution improves with this modification
        if (swapGain(solution, points, weights, x, y) > 0) {

          // swap points
          solution.remove(y);
          solution.put(x, x);

          // end modification step
          return true;
        }
      }
    }
  }

  // change in objcetive from performing a swap
  private float swapGain(TreeMap<Integer, Integer> solution,
                         TreeMap<Integer, float[]> points,
                         TreeMap<Integer, Float> weights,
                         int newPoint,
                         int oldPoint) {

    float obj1 = this.solutionObjective(solution, points, weights);

    solution.remove(oldPoint);
    solution.put(newPoint, newPoint);

    float obj2 = this.solutionObjective(solution, points, weights);

    solution.remove(newPoint);
    solution.put(oldPoint, oldPoint);

    return obj2 - obj1;
  }

  // find the cost of a solution
  private float solutionObjective(TreeMap<Integer, Integer> solution,
                                  TreeMap<Integer, float[]> points,
                                  TreeMap<Integer, Float> weights) {

    float obj = 0;

    // create an iterator of the points
    Iterator<Integer> pointIterator = points.iterator();

    // find the service cost of each point
    while (pointIterator.hasNext()) {

      int x = pointIterator.next();

      float costX = Float.POSITIVE_INFINITY;

      // create an iterator for points in solution
      Iterator<Integer> solutionIterator = solution.iterator();

      while (solutionIterator.hasNext()) {
        int y = solutionIterator.next();
        float d = metric.d(points.get(x), points.get(y));

        if (d < costX) costX = d;
      }

      obj += weights.get(x)*costX;
    }
    return obj;
  }

}
