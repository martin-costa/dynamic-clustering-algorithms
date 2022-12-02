import java.util.*;
import java.io.*;

/*

an implementation of the kmeans++ algorithm

*/

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

  // number of post processing iterations
  private int iterations;

  // the clusters we want to find
  private TreeMap<Integer, Integer>[] clusters;

  // the centers of these clusters
  private int[] clusterCenters;

  // set the metric and the value k
  KMeansPlusPlus(int k, Metric metric, int iterations) {
    this.metric = metric;
    this.k = k;
    this.iterations = iterations;
  }

  KMeansPlusPlus(int k, Metric metric) {
    this.metric = metric;
    this.k = k;
    this.iterations = 2;
  }

  public TreeMap<Integer, Integer> cluster(TreeMap<Integer, float[]> points) {

    // set all the weights to be 1
    TreeMap<Integer, Float> weights = new TreeMap<Integer, Float>();

    Set<Integer> keySet = points.keySet();

    for (Integer key : keySet) {
      weights.put(key, 1.0f);
    }

    return cluster(points, weights);
  }

  public TreeMap<Integer, Integer> cluster(TreeMap<Integer, float[]> points, TreeMap<Integer, Float> weights) {

    // set the points and the weights
    this.points = points;
    this.weights = weights;

    // the number of points
    this.n = points.size();

    // set the dimension of the data
    if (this.n >= 0) {
      this.d = points.get(points.firstKey()).length;
    }

    return kmeansplusplus(iterations);
  }

  // implementation of bicriteria approximation using kmeans++
  public TreeMap<Integer, Integer> kmeansplusplus(int iterations) {

    // if we have at most k points return each point as a center
    if (n <= k) {
      return returnAll();
    }

    // create the treemaps that define the clusters
    @SuppressWarnings("unchecked")
    TreeMap<Integer, Integer>[] clusterMaps = new TreeMap[k];

    this.clusters = clusterMaps;

    // seed good starting centers and create clusters
    seedStartingCenters();

    // run k iterations of kmeans
    kmeans(iterations);

    // get the centers from the clusters and return them
    TreeMap<Integer, Integer> solution = new TreeMap<Integer, Integer>();

    // put cluster centers
    this.clusterCenters = new int[k];

    for (int i = 0; i < k; i++) {
      int p = getClusterCenter(i);
      solution.put(p, p);
      this.clusterCenters[i] = p;
    }

    return solution;
  }

  // create a trivial solution if n is too small
  private TreeMap<Integer, Integer > returnAll() {

    // create the treemaps that define the clusters
    @SuppressWarnings("unchecked")
    TreeMap<Integer, Integer>[] clusterMaps = new TreeMap[n];

    this.clusters = clusterMaps;

    // create the clusters and centers
    this.clusterCenters = new int[n];

    // create solution
    TreeMap<Integer, Integer> solution = new TreeMap<Integer, Integer>();

    Integer[] pointsArr = points.keySet().toArray(new Integer[0]);

    int i = 0;
    for (Integer key : pointsArr) {
      solution.put(key, key);
      this.clusters[i] = new TreeMap<Integer, Integer>();
      this.clusters[i].put(key, key);
      this.clusterCenters[i] = key;
      i++;
    }
    return solution;
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

    float[][] samplePoints = new float[k][d];

    for (int i = 0; i < k; i++) {
      samplePoints[i] = points.get(pointsArr[dSquaredWeighting(rng, probs, dist, pointsArr, weightsArr)]);
    }

    // create the initial clusters
    createClusters(samplePoints);
  }

  // get the point in cluster i closest to the center of mass of cluster i
  private int getClusterCenter(int i) {

    float[] centerOfMass = clusterCenterOfMass(i);

    Integer[] clusterPoints = clusters[i].keySet().toArray(new Integer[0]);

    int closestPoint = -1;
    float dist = Float.POSITIVE_INFINITY;

    for (Integer key : clusterPoints) {
      float d = metric.d(points.get(key), centerOfMass);
      if (d < dist) {
        closestPoint = key;
        dist = d;
      }
    }

    return closestPoint;
  }

  // sample a point according to D^2 weighting
  private int dSquaredWeighting(Random rng, float[] probs, float[] dist, Integer[] pointsArr, float[] weightsArr) {

    float r = rng.nextFloat();
    float s = 0;

    int sample = 0;

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

    // if every points is already at a point thats been sampled
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
    }

    // create the new clusters
    createClusters(newCenters);
  }

  // given the new centers create the clusters
  private void createClusters(float[][] newCenters) {

    // reset the clusters
    for (int i = 0; i < k; i++) {
      clusters[i] = new TreeMap<Integer, Integer>();
    }

    // get an array of the keys of all the points
    Integer[] pointsArr = points.keySet().toArray(new Integer[0]);

    // re-allocate points to clusters
    for (int i = 0; i < n; i++) {

      float dist = Float.POSITIVE_INFINITY;
      int l = 0;

      for (int j = 0; j < k; j++) {

        float d = metric.d(newCenters[j], points.get(pointsArr[i]));

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

  // get the clusters
  public TreeMap<Integer, Integer>[] getClusters() {
    return clusters;
  }

  // get the cluster centers
  public int[] getClusterCenters() {
    return clusterCenters;
  }
}
