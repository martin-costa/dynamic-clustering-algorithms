import java.util.*;
import java.io.*;

/*

implementation of balanced binary tree DS used in Henzinger et al. dynamic coreset

*/

// the blanaced binary tree used to mantain a dynmaic coreset
public class HenzingerTree {

  // the root node
  private Node root;

  // pointer to the leaf where the next leaf will be added
  private Node insertionPointer;

  // pointers to the leaves that cointain each point
  private TreeMap<Integer, Leaf> leafFinder;

  // the size of this balanced binary tree
  private int n;

  // save parameters to feed to new nodes when we create them
  private int k;
  private Metric metric;

  public HenzingerTree(int k, Metric metric) {
    this.k = k;
    this.metric = metric;
    this.n = 0;
    this.leafFinder = new TreeMap<Integer, Leaf>();
  }

  // insert a point (we only care about unweighted points)
  public void insert(int key, float[] point) {

    // create a new node for our
    //nodeFinder.put(key, newNode);

  }

  // delete a point from the tree
  public void delete(int key) {

  }

  // move refresher pointer
  public void refresher() {

  }

}

// class to define nodes for the Henzinger tree
abstract class Node {

  // parent of this node
  public Internal parent;

  // parameters for clustering
  protected int k;
  protected Metric metric;

  // retrives the set of points in the subtree at this node
  public abstract TreeMap<Integer, float[]> getPoints();

  // retrives the set of weights of the points in the subtree at this node
  public abstract TreeMap<Integer, Float> getWeights();

  // recomputes all notes from here to root
  public abstract void recomputeUpwards();
}

// create lass for internal (non-outer ALG nodes) nodes
class Internal extends Node {

  // left child of this node
  public Node left;

  // right child of this node
  public Node right;

  // the weighted set maintained as the output of this node
  private TreeMap<Integer, float[]> outPoints;
  private TreeMap<Integer, Float> outWeights;

  Internal(int k, Metric metric) {
    this.k = k;
    this.metric = metric;
  }

  // continute the recomputation
  public void recomputeUpwards() {

    // recompute the coreset at this node
    recompute();

    parent.recomputeUpwards();
  }

  // recompute this node
  public void recompute() {

  }

  // return coreset output points
  public TreeMap<Integer, float[]> getPoints() {
    return outPoints;
  }

  // return coreset output weights
  public TreeMap<Integer, Float> getWeights() {
    return outWeights;
  }

  // retrieves the leftmost leaf node of the tree
  public Node leftmostLeaf() {
    if (left != null)
      return left;
    return this;
  }
}

// class for leaf nodes
class Leaf extends Node {

  // the key of the point at this leaf
  private int key;

  // the point at this leaf
  private float[] point;

  // gets the next leaf
  public Leaf next;

  // gets the previous leaf
  public Leaf last;

  Leaf(int k, Metric Metric, int key, float[] point) {
    this.k = k;
    this.metric = metric;
    this.key = key;
    this.point = point;
  }

  // turns leaf into an internal node and adds and returns a new leaf
  public Leaf insert(int key, float[] point, int n, int s, float lamba, float epsilon) {

    // create new leaf
    Leaf leaf = new Leaf(k, metric, key, point);

    // set the next and right pointers
    leaf.last = this;
    leaf.next = this.next;
    this.next.last = leaf;
    this.next = leaf;

    // create new internal node
    Internal internal = new Internal(k, metric);

    // set internal nodes parent and children
    internal.parent = this.parent;
    internal.left = this;
    internal.right = leaf;

    // set this parent
    this.parent = internal;

    // recompute the node to leaf path from this.next
    this.next.recomputeUpwards();

    return leaf;
  }

  // remove and return this leaf from the tree and replaces parent with a leaf
  // NOTE: we assume in this method that the the tree is balanced and we are
  // deleting the last leaf on the lowest layer of the tree!
  public Leaf delete() {

    // the leafs sibling
    Leaf leaf = (Leaf)this.parent.left;

    // set grandparents right to left
    this.parent.parent.right = leaf;

    // fix leafs next pointer
    leaf.next = this.next;

    return this;
  }

  // return the single point
  public TreeMap<Integer, float[]> getPoints() {

    TreeMap<Integer, float[]> points = new TreeMap<Integer, float[]>();
    points.put(key, point);

    return points;
  }

  // return a weight of 1
  public TreeMap<Integer, Float> getWeights() {

    TreeMap<Integer, Float> weights = new TreeMap<Integer, Float>();
    weights.put(key, 1.0f);

    return weights;
  }

  // continute the recomputation
  public void recomputeUpwards() {
    parent.recomputeUpwards();
  }
}
