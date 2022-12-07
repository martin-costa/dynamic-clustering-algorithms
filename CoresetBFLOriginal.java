import java.util.*;
import java.io.*;

/*

implementation of (static) coreset construction by Braverman et al. 2016

*/

public class CoresetBFL {

  // parameter for k clustering
  private int k;

  // metric used to evaluate distances
  private Metric metric;

  // the paramter rho of the rho-metric space
  private float rho;

  // algorithm and factors for the (alpha, beta)-approximation
  private KMeansPlusPlus kmeanspp;

  private float alpha;
  private float beta;

  // store the (alpha, beta)-approximation
  private TreeMap<Integer, Integer>[] clusters;
  private int[] clusterCenters;

  // the input points we compute a coreset on
  private TreeMap<Integer, float[]> points;

  private TreeMap<Integer, Float> weights;

  // the coreset we compute
  private TreeMap<Integer, float[]> outPoints;

  private TreeMap<Integer, Float> outWeights;

  public CoresetBFL(int k, Metric metric, float rho) {
    this.k = k;
    this.metric = metric;

    this.rho = rho;

    this.kmeanspp = new KMeansPlusPlus(k, metric);

    //this.alpha = 8*((float)Math.log(k) + 2);
    this.alpha = (float)Math.log(k);
    this.beta = 1;
  }

  // returns an epsilon-coreset given a weighted set (P, w) of a rho-metric space
  // and an (alpha, beta)-approximation P --> B
  public void construct(TreeMap<Integer, float[]> points, TreeMap<Integer, Float> weights, float lambda, float epsilon) {

    this.points = points;
    this.weights = weights;

    int n = this.points.size();

    // compute the coreset
    float t = (float)Math.max(1, Math.log(k)/10);
    float c = (float)Math.max(1/Math.pow(epsilon, 2), 1);

    float m1 = c*k*(t + 1);
    float m2 = (float)(Math.log(n)*Math.log(t) + Math.log(beta*k) + Math.log(1/lambda));

    int m = (int)(m1*m2);

    m = 1000;

    // if there aren't enough points
    if (n < m) {
      outPoints = points;
      outWeights = weights;
      return;
    }

    // get an (alpha, beta)-approximation
    approximate();

    // proceed with coreset construction
    coreset(m);
  }

  // constructs the coreset with m samples
  private void coreset(int m) {

    int n = this.points.size();

    // create arrays of point keys for each cluster
    Integer[][] clusterArrays = new Integer[k][];

    for (int i = 0; i < k; i++) {
      clusterArrays[i] = clusters[i].keySet().toArray(new Integer[0]);
    }

    // arrays of total weight per cluster
    float[] clusterWeights = new float[k];

    // total cost the approximation
    float v = 0;

    for (int i = 0; i < k; i++) {
      clusterWeights[i] = 0;
      for (int j = 0; j < clusterArrays[i].length; j++) {
        clusterWeights[i] += weights.get(clusterArrays[i][j]);
        v += weights.get(clusterArrays[i][j])*metric.d(points.get(clusterArrays[i][j]), points.get(clusterCenters[i]));
      }
    }

    // the arrays of sampling probabilities
    float[][] prob = new float[k][];

    for (int i = 0; i < k; i++) {
      prob[i] = new float[clusterArrays[i].length];
      for (int j = 0; j < prob[i].length; j++) {
        prob[i][j] = 0.5f*weights.get(clusterArrays[i][j])*metric.d(points.get(clusterArrays[i][j]), points.get(clusterCenters[i]))/v;
        prob[i][j] += 0.5f*weights.get(clusterArrays[i][j])/(k*clusterWeights[i]);
      }
    }

    samplePoints(m, prob, clusterArrays);
  }

  public void samplePoints(int m, float[][] prob, Integer[][] clusterArrays) {

    outPoints = new TreeMap<Integer, float[]>();
    outWeights = new TreeMap<Integer, Float>();

    Random rng = new Random();

    // sample m points and place them into the corset output
    for (int l = 0; l < m; l++) {

      float r = rng.nextFloat();
      float t = 0;

      // sample a point
      for (int i = 0; i < k; i++) {

        int len = prob[i].length;

        for (int j = 0; j < len; j++) {

          t += prob[i][j];
          if (r <= t) {
            int key = clusterArrays[i][j];
            outPoints.put(key, points.get(key));
            outWeights.put(key, weights.get(key)/(m*prob[i][j]));

            // break from loop
            j = len;
            i = k;
          }
        }
      }
    }
  }

  // computes an (alpha, beta)-approximation using kmeans++
  public void approximate() {

    // compute the (alpha beta)-approximation
    kmeanspp.cluster(points, weights);

    clusters = kmeanspp.getClusters();
    clusterCenters = kmeanspp.getClusterCenters();
  }

  // returns the points computed by the coreset
  public TreeMap<Integer, float[]> getPoints() {
    return outPoints;
  }

  // returns the weights computed by the coreset
  public TreeMap<Integer, Float> getWeights() {
    return outWeights;
  }


}
