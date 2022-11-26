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

  // determines coreset quality
  private float epsilon;

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

  public CoresetBFL(int k, Metric metric, float rho, float epsilon) {
    this.k = k;
    this.metric = metric;

    this.rho = rho;
    this.epsilon = epsilon;

    this.kmeanspp = new KMeansPlusPlus(k, metric);
  }

  // returns an epsilon-coreset given a weighted set (P, w) of a rho-metric space
  // and an (alpha, beta)-approximation P --> B
  public void construct() {

    // get an (alpha, beta)-approximation
    approximate();

    // compute the coreset

  }

  // computes an (alpha, beta)-approximation using kmeans++
  public void approximate() {

    this.alpha = 8*((float)Math.log(k) + 2);
    this.beta = 1;

    // compute the (alpha beta)-approximation
    kmeanspp.cluster(points);

    TreeMap<Integer, Integer>[] clusters = kmeanspp.getClusters();
    int[] clusterCenters = kmeanspp.getClusterCenters();
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
