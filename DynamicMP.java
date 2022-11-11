import java.lang.Math;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.*;

/*

implementation of our dynamic data structure for maintaining a coreset

*/

// our dynamic coreset data structure
public class DynamicMP {

  // the underlying metric space
  private TreeMap<Integer, float[]> space;

  // the nested collections of points for each layer
  private LinkedList<TreeMap<Integer, Integer>> layers;

  // the sampled medians for each layer
  private LinkedList<TreeMap<Integer, Integer>> samples;

  // the clusters for each layer
  private LinkedList<TreeMap<Integer, Integer>[]> clusters;

  // number of updates since reconstruction at each layer
  private LinkedList<Integer> reconTimer;

  // metric used to evaluate distances
  private Metric metric;

  // parameters for layer construction
  private int k;

  private float alpha;
  private float beta;
  private float epsilon;
  private float tau;

  // number of points taken as medians each layer
  private int sampleSize;

  public DynamicMP(int k, Metric metric, float alpha, float beta, float epsilon) {

    // the metric
    this.metric = metric;

    // parameters
    this.k = k;
    this.alpha = alpha;
    this.beta = beta;
    this.epsilon = epsilon;
    this.tau = beta*epsilon;

    // sampling parameter
    this.sampleSize = (int)Math.floor(alpha*k);

    // initialise the data structures
    this.space = new TreeMap<Integer, float[]>();

    this.layers = new LinkedList<TreeMap<Integer, Integer>>();

    this.layers.add(new TreeMap<Integer, Integer>());

    this.samples = new LinkedList<TreeMap<Integer, Integer>>();

    this.clusters = new LinkedList<TreeMap<Integer, Integer>[]>();

    this.reconTimer = new LinkedList<Integer>();
  }

  /*

  insert a point into the data structure

  */

  public void insert(int key, float[] point) {

    // insert point into the metric space
    this.space.put(key, point);

    // insert point into each layer
    ListIterator<TreeMap<Integer, Integer>> layerIterator = layers.listIterator();
    ListIterator<Integer> reconIterator = reconTimer.listIterator();

    for (int i = 0; i < this.depth()-1; i++) {

      // insert point into layer
      layerIterator.next().put(key, key);

      // decrement reconstruction timer for this layer
      reconIterator.set(reconIterator.next()-1);
    }

    // insert point into unsampled layer
    layerIterator.next().put(key, key);

    this.checkForReconstruction();
  }

  /*

  delete a point in the data structure

  */

  public void delete(int key) {

    // delete point from the metric space
    this.space.remove(key);

    // delete point from each layer
    ListIterator<TreeMap<Integer, Integer>> layerIterator = this.layers.listIterator();
    ListIterator<Integer> reconIterator = this.reconTimer.listIterator();

    for (int i = 0; i < this.depth()-1; i++) {

      // delete point from layer
      int clusterIndex = layerIterator.next().remove(key);

      // decrement reconstruction timer for this layer
      reconIterator.set(reconIterator.next()-1);

      // check if point is in a cluster at this layer
      if (clusterIndex >= 0) {

        // get the cluster and set of sampled points
        TreeMap<Integer, Integer> cluster = this.clusters.get(i)[clusterIndex];
        TreeMap<Integer, Integer> layerSamples = this.samples.get(i);

        // remove point from this cluster
        cluster.remove(key);

        // check if the point was sampled in this layer
        if (layerSamples.containsKey(key)) {

          // remove the point from the samples
          layerSamples.remove(key);

          // replace sampled point with an arbitrary point in the cluster
          Integer newCenter = cluster.firstKey();
          if (newCenter != null) layerSamples.put(newCenter, newCenter);
        }

        // check if data structure needs to be reconstructed
        this.checkForReconstruction();
        return;
      }
    }

    // remove point from unslampled layer
    layerIterator.next().remove(key);

    // check if data structure needs to be reconstructed
    this.checkForReconstruction();
  }

  // check whether some layer needs to be reconstructed
  private void checkForReconstruction() {

    for (int i = 0; i < this.reconTimer.size(); i++) {
      if (this.reconTimer.get(i) <= 0) {

        // reconstruct data structure starting from layer i
        reconstructFromLayer(i);
        return;
      }
    }
  }

  // reconstruct the layers starting from layer i
  private void reconstructFromLayer(int i) {

    // delete the lower layers
    while (this.depth() - 1 > i) {
      this.layers.removeLast();
      this.samples.removeLast();
      this.clusters.removeLast();
      this.reconTimer.removeLast();
    }

    // construct the new layers
    while(this.layers.getLast().size() > this.sampleSize) {
      this.constructLayer();
    }
  }

  // creates a new layer
  private void constructLayer() {

    // place the points contained in the unsampled layer in an array
    Integer[] points = this.layers.getLast().keySet().toArray(new Integer[0]);

    int n = points.length;
    Random rng = new Random();

    // sample points as new centers from this set
    TreeMap<Integer, Integer> layerSamples = new TreeMap<Integer, Integer>();

    for (int i = 0; i < this.sampleSize; ) {

    }


  }

  // returns the number of layers in the data structure
  public int depth() {
    return this.layers.size();
  }

}
