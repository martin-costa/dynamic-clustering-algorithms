import java.util.*;
import java.io.*;

/*

implementation of (static) coreset construction by Braverman et al. 2016

*/

public class CoresetBFL {

  public CoresetBFL() {

  }

  // returns an epsilon-coreset given a weighted set (P, w) of a rho-metric space
  // and an (alpha, beta)-approximation s : P --> B
  public void construct() {



  }


}

// an implementation of the kmeans++ algorithm
class KMeansPlusPlus {

  // the points that we want to cluster
  private TreeMap<Integer, float[]> points;

  // the weights of the points to be clustered
  private TreeMap<Integer, Float> weights;

  // the metric
  private Metric metric;

  // the paramter k
  private int k;

  // the number of points
  private int n;

  // dimension of the data
  private int d;

  // the clusters we want to find
  private TreeMap<Integer, Integer>[] clusters;

  // set the points and weights
  KMeansPlusPlus(TreeMap<Integer, float[]> points, TreeMap<Integer, Float> weights, Metric metric, int k) {
    this.points = points;
    this.weights = weights;
    this.metric = metric;
    this.k = k;

    // the number of points
    this.n = points.size();

    // set the dimension of the data
    if (this.n >= 0) {
      this.d = points.get(points.firstKey()).length;
    }
  }

  // implementation of bicriteria approximation using kmeans++
  public TreeMap<Integer, Integer>[] kmeansplusplus(int iterations) {

    // create the treemaps that define the clusters
    @SuppressWarnings("unchecked")
    TreeMap<Integer, Integer>[] clusterMaps = new TreeMap[k];

    this.clusters = clusterMaps;

    // RETURN IF n IS SMALL

    // seed good starting centers
    seedStartingCenters();

    // run k iterations of kmeans
    kmeans(iterations);

    return clusters;
  }

  // find a good starting point for kmeans
  private void seedStartingCenters() {

    // get an array of the keys of all the points
    Integer[] pointsArr = points.keySet().toArray(new Integer[0]);

    // get an array of the keys of all the points
    float[] weightsArr = new float[n];

    // distances from samples points
    float[] dist = new float[n];
    Arrays.fill(dist, Float.POSITIVE_INFINITY);

    // total weight of points
    float totalWeight = 0;

    for (int i = 0; i < n; i++) {
      weightsArr[i] = weights.get(pointsArr[i]);
      totalWeight += weightsArr[i];
    }

    // create array with sampling probabilities
    float[] probs = new float[n];

    for (int i = 0; i < n; i++) {
      probs[i] = weightsArr[i]/totalWeight;
    }

    // create random number generator
    Random rng = new Random();

    int[] samples = new int[k];

    for (int i = 0; i < k; i++) {
      samples[i] = pointsArr[dSquaredWeighting(rng, probs, dist, pointsArr, weightsArr)];
    }

  }

  // sample a point according to D^2 weighting
  private int dSquaredWeighting(Random rng, float[] probs, float[] dist, Integer[] pointsArr, float[] weightsArr) {

    float r = rng.nextFloat();
    float s = 0;

    int sample = -1;

    // sample a point from the distribution defined by probs
    for (int i = 0; i < n; i++) {
      s += probs[i];
      if (r <= s) {
        sample = i;
        break;
      }
    }

    // compute the new distances and probabilities
    float totalDSquared = 0;

    for (int i = 0; i < n; i++) {
      dist[i] = Math.min(dist[i], metric.d(points.get(pointsArr[i]), points.get(pointsArr[sample])));
      totalDSquared += weightsArr[i]*dist[i]*dist[i];
    }

    // we every points is already at a point thats been sampled
    if (totalDSquared <= 0) {
      probs[0] = 1;
      for (int i = 1; i < n; i++) {
        probs[i] = 0;
      }
      return sample;
    }

    for (int i = 0; i < n; i++) {
      probs[i] = weightsArr[i]*dist[i]*dist[i]/totalDSquared;
    }

    return sample;
  }

  // standard kmeans
  private void kmeans(int iterations) {

    // run iterations many interations of kmeans heuristic
    for (int i = 0; i < iterations; i++) {
      kmeansIteration();
    }
  }

  // one iteration of kmeans
  private void kmeansIteration() {

    // find the centers of mass
    float[][] newCenters = new float[k][d];

    for (int i = 0; i < k; i++) {

      // get cluster center of mass
      newCenters[i] = clusterCenterOfMass(i);

      // reset the cluster
      clusters[i] = new TreeMap<Integer, Integer>();
    }

    // get an array of the keys of all the points
    Integer[] pointsArr = points.keySet().toArray(new Integer[0]);

    // re-allocate points to clusters
    for (int i = 0; i < n; i++) {

      float dist = Float.POSITIVE_INFINITY;
      int l = 0;

      for (int j = 0; j < k; j++) {

        float d = metric.d(newCenters[i], points.get(pointsArr[i]));

        // is point is closer to cluster center j than l
        if (d < dist) {
          dist = d;
          l = j;
        }
      }

      // place point in cluster l
      clusters[l].put(pointsArr[i], pointsArr[i]);
    }
  }

  // returns the center of mass of cluster i
  private float[] clusterCenterOfMass(int i) {

    // the center of mass
    float[] center = new float[d];

    // the points in the cluster
    Integer[] cluster = clusters[i].keySet().toArray(new Integer[0]);

    // total weight of points in this cluster
    float totalWeight = 0;

    for (Integer p : cluster) {

      // get the point and its weight
      float[] x = points.get(p);
      float w = weights.get(p);

      totalWeight += w;

      for (int j = 0; j < d; j++) {
        center[j] += w*x[j];
      }
    }

    for (int j = 0; j < d; j++) {
      center[j] /= totalWeight;
    }

    return center;
  }

}
