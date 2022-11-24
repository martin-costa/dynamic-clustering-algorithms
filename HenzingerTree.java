import java.util.*;
import java.io.*;

/*

implementation of balanced binary tree DS used in Henzinger et al. dynamic coreset

*/

// the blanaced binary tree used to mantain a dynmaic coreset
public class HenzingerTree {

  // the root node of the tree
  private Node root;

  // pointer to the node where the next leaf will be added
  private Node insertionPointer;

  // pointers to the nodes that cointain each point
  private TreeMap<Integer, Node> nodeFinder;

  // the size of this balanced binary tree
  private int n;

  // save parameters to feed to new nodes when we create them
  private int k;
  private Metric metric;

  public HenzingerTree(int k, Metric metric) {

    this.root = new Node(k, metric);
    this.n = 0;

    this.insertionPointer = root;

    this.nodeFinder = new TreeMap<Integer, Node>();

    this.k = k;
    this.metric = metric;
  }

  // insert a point (we only care about unweighted points)
  public void insert(int key, float[] point) {

    // create a new node for our point
    Node newNode = new Node(k, metric);

    // create a new node for our 
    nodeFinder.put(key, newNode);

  }

  public void delete(int key) {

  }

}

// create node class for binary tree used by HenzingerCore
class Node {

  // parent of this node
  private Node parent;

  // left child of this node
  private Node left;

  // right child of this node
  private Node right;

  // threshold for maximum output size
  private int s;

  // the paramter k
  private int k;

  // the metric
  private Metric metric;

  // the weighted set maintained as the output of this node
  private TreeMap<Integer, float[]> outPoints;
  private TreeMap<Integer, Float> outWeights;

  // the union of the weighted sets of its children
  private TreeMap<Integer, float[]> inPoints;
  private TreeMap<Integer, Float> inWeights;

  Node(int k, int Metric) {
    this.k = k;
    this.metric = metric;
  }

  // retrieves the leftmost leaf node of the tree
  public Node leftmostLeaf() {
    if (left != null)
      return left;
    return this;
  }
}
