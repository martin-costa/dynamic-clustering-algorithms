import java.lang.Math;
import java.util.LinkedList;
import java.util.TreeMap;

/*

implementation of dynamic data structure for maintaining a coreset

*/

// our dynamic coreset data structure
public class DynamicMP {

  // the nested collections of points for each layer
  private LinkedList<TreeMap<Integer, float[]>> U;

  // the sampled medians for each layer
  private LinkedList<TreeMap<Integer, Integer>> S;

  // the clusters for each layer
  private LinkedList<TreeMap<Integer, Integer>[]> C;

  // parameters for layer construction
  private int k;

  private float alpha;
  private float beta;

  private int sampleSize;

  public DynamicMP(int k, float alpha, float beta) {

    // parameters
    this.k = k;
    this.alpha = alpha;
    this.beta = beta;

    // sampling parameter
    this.sampleSize = (int)Math.floor(alpha*k);
  }

  // insert a point into the data structure
  public void insert(int key, float[] data) {

  }

  // delete a point from the data structure
  public void delete(int key) {

  }

}
