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
    seedStaringCenters();

    // run k iterations of kmeans
    kmeans(iterations);

    return clusters;
  }

  // find a good starting point for kmeans
  public void seedStaringCenters() {

  }

  // standard kmeans
  public void kmeans(int iterations) {

    // run iterations many interations of kmeans heuristic
    for (int i = 0; i < iterations; i++) {
      kmeansIteration();
    }
  }

  // one iteration of kmeans
  public void kmeansIteration() {

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
  public float[] clusterCenterOfMass(int i) {

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
