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
  // NOTE: the location of the next deletion is deletionPoint.last
  private Leaf insertionPoint;

  // the refresh pointers
  private Leaf refreshPointer;

  // pointers to the leaves that cointain each point
  private TreeMap<Integer, Leaf> leafFinder;

  // the size of this balanced binary tree
  private int n;

  // save parameters to feed to new nodes when we create them
  private int k;

  private Metric metric;

  private float epsilon;

  // phase parameters
  private int np;
  private int phaseCounter;

  // the output of the final coreset
  private TreeMap<Integer, float[]> outPoints;
  private TreeMap<Integer, Float> outWeights;

  private CoresetBFL outercore;

  public HenzingerTree(int k, Metric metric, float epsilon) {
    this.k = k;
    this.metric = metric;
    this.epsilon = epsilon;

    this.leafFinder = new TreeMap<Integer, Leaf>();
    this.n = 0;

    this.np = 0;
    this.phaseCounter = 0;

    this.outercore = new CoresetBFL(k, metric, 1);

    this.outPoints = new TreeMap<Integer, float[]>();
    this.outWeights = new TreeMap<Integer, Float>();
  }

  // insert a point (we only care about unweighted points)
  public void insert(int key, float[] point) {

    refresher();

    // if this point is already in the tree, do nothing
    if (leafFinder.get(key) != null) return;

    // create new leaf
    Leaf leaf = new Leaf(k, metric, key, point);

    // check if the tree is empty
    if (n == 0) {

      leaf.next = leaf;
      leaf.last = leaf;

      root = leaf;

      // the insertion point is the root
      insertionPoint = leaf;

      // add root to map
      leafFinder.put(key, leaf);

      n = 1;
    }
    else {

      // insert the point
      Internal internal = insertionPoint.insert(leaf, np, lambda(), epsilon());

      if (n == 1) {
        root = internal;
      }

      // add new leaf to map
      leafFinder.put(key, leaf);

      // reset the position of the insertion pointer
      insertionPoint = leaf.next;

      n += 1;
    }

    outerInstance();
  }

  // delete a point from the tree
  public void delete(int key) {

    refresher();

    // get the node contining the key to be deleted
    Leaf leafToReplace = leafFinder.get(key);

    // if this point is not in the tree, do nothing
    if (leafToReplace == null) return;

    // if the tree will be empty after this update, delete it
    if (n == 1) {

      // set the root and insertionPoint to null
      root = null;
      insertionPoint = null;

      leafFinder.remove(key);

      n = 0;
    }
    else {

      // delete the last leaf in the tree
      Leaf deadLeaf = insertionPoint.last.delete(np, lambda(), epsilon());

      // reset the position of the insertion pointer
      insertionPoint = insertionPoint.last;

      // remove dead leaf from leafFinder
      leafFinder.remove(deadLeaf.key());

      n -= 1;

      // if we have deleted the correct leaf
      if (deadLeaf.key() != key) {
        leafToReplace.replace(deadLeaf.key(), deadLeaf.point(), np, lambda(), epsilon());
        leafFinder.put(deadLeaf.key(), leafToReplace);
      }
    }

    outerInstance();
  }

  // move refresher pointer
  private void refresher() {

    phaseCounter--;

    // start new phase
    if (phaseCounter <= 0) {
      np = 4*n;
      phaseCounter = n/2;
      refreshPointer = insertionPoint;
    }

    if (phaseCounter <= 0 || refreshPointer == null) {
      return;
    }

    // use refresh pointer
    refreshPointer.recomputeUpwards(np, lambda(), epsilon());
    refreshPointer = refreshPointer.next;
    refreshPointer.recomputeUpwards(np, lambda(), epsilon());
    refreshPointer = refreshPointer.next;
  }

  // computes the final output corset of smaller size
  private void outerInstance() {

    if (root == null) {
      this.outPoints = new TreeMap<Integer, float[]>();
      this.outWeights = new TreeMap<Integer, Float>();
      return;
    }

    TreeMap<Integer, float[]> inPoints = root.getPoints();
    TreeMap<Integer, Float> inWeights = root.getWeights();

    //System.out.println(inPoints.size());

    // run the outercore
    outercore.construct(inPoints, inWeights, 1.0f/(n + 1), epsilon);

    //System.out.println(outercore.getPoints().size());

    outPoints = outercore.getPoints();
    outWeights = outercore.getWeights();
  }

  // cluster the points in the corset
  public TreeMap<Integer, Integer> cluster() {

    // call the static algorithm on the outercore output
    OnlineKMedian staticAlgo = new OnlineKMedian(k, metric);

    return staticAlgo.cluster(outPoints, outWeights);
  }

  // the parameter lambda for inner ALG instances
  private float lambda() {
    return 1.0f/(2.0f*(np*np + 1));
  }

  // the paramter epsilon for inner ALG instances
  private float epsilon() {
    return this.epsilon/(6.0f*(float)Math.log(np));
  }

  // returns the points computed by the tree
  public TreeMap<Integer, float[]> getPoints() {
    return outPoints;
  }

  // returns the weights computed by the tree
  public TreeMap<Integer, Float> getWeights() {
    return outWeights;
  }

  // print for debugging
  public void print() {
    if (root != null)
      root.print();
  }
}

/*

abstract class to define nodes for the Henzinger tree

*/

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
  public abstract void recomputeUpwards(int n, float lambda, float epsilon);

  // for debugging
  public abstract void print();
}

/*

create class for internal (non-outer ALG nodes) nodes

*/

// create class for internal (non-outer ALG nodes) nodes
class Internal extends Node {

  // left child of this node
  public Node left;

  // right child of this node
  public Node right;

  // the weighted set maintained as the output of this node
  private TreeMap<Integer, float[]> outPoints;
  private TreeMap<Integer, Float> outWeights;

  // the coreset maintained here
  private CoresetBFL innercore;

  Internal(int k, Metric metric) {
    this.k = k;
    this.metric = metric;

    outPoints = new TreeMap<Integer, float[]>();
    outWeights = new TreeMap<Integer, Float>();

    innercore = new CoresetBFL(k, metric, 1);
  }

  // continue the recomputation
  public void recomputeUpwards(int n, float lambda, float epsilon) {

    // recompute the coreset at this node
    recompute(n, lambda, epsilon);

    if (parent != null)
      parent.recomputeUpwards(n, lambda, epsilon);
  }

  // run the static coreset algorithm on union of inputs
  public void recompute(int n, float lambda, float epsilon) {

    // get the union of the inputs
    TreeMap<Integer, float[]> inPoints = new TreeMap<Integer, float[]>();
    TreeMap<Integer, Float> inWeights = new TreeMap<Integer, Float>();

    inPoints.putAll(left.getPoints());
    inPoints.putAll(right.getPoints());

    inWeights.putAll(left.getWeights());
    inWeights.putAll(right.getWeights());

    // compute the coreset
    innercore.construct(inPoints, inWeights, lambda, epsilon);

    outPoints = innercore.getPoints();
    outWeights = innercore.getWeights();
  }

  // return coreset output points
  public TreeMap<Integer, float[]> getPoints() {
    return outPoints;
  }

  // return coreset output weights
  public TreeMap<Integer, Float> getWeights() {
    return outWeights;
  }

  // METHOD FOR DEBUGGING
  public void print() {
    left.print();
    right.print();
  }
}

/*

create class for leaf nodes

*/

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

  Leaf(int k, Metric metric, int key, float[] point) {
    this.k = k;
    this.metric = metric;
    this.key = key;
    this.point = point;
  }

  // turns leaf into an internal node and adds and returns a new leaf
  public Internal insert(Leaf leaf, int n, float lambda, float epsilon) {

    // set the next and right pointers
    leaf.last = this;
    leaf.next = this.next;
    this.next.last = leaf;
    this.next = leaf;

    // create new internal node
    Internal internal = new Internal(k, metric);

    // set the pointer of the parent node
    if (this.parent != null) {
      if (this.parent.left.equals(this)) {
        this.parent.left = internal;
      }
      else {
        this.parent.right = internal;
      }
    }

    // set internal nodes parent and children
    internal.parent = this.parent;
    internal.left = this;
    internal.right = leaf;

    // set this and leaf's parent
    this.parent = internal;
    leaf.parent = internal;

    // recompute the node to leaf path from this.next
    this.next.recomputeUpwards(n, lambda, epsilon);

    // return the new leaf
    return internal;
  }

  // remove this leaf from the tree and replaces parent with its sibling
  // NOTE: we assume in this method that the the tree is balanced and we are
  // deleting the last leaf on the lowest layer of the tree!
  public Leaf delete(int n, float lambda, float epsilon) {

    // the leafs sibling
    Leaf leaf = (Leaf)this.parent.left;

    // set the next and right pointers
    leaf.next = this.next;
    this.next.last = leaf;

    // set pointer of grandparent to leaf
    if (this.parent.parent != null) {
      if (this.parent.parent.left.equals(this.parent)) {
        this.parent.parent.left = leaf;
      }
      else {
        this.parent.parent.right = leaf;
      }
    }

    // set leafs parent to its grandparent
    leaf.parent = leaf.parent.parent;

    // recompute the node to leaf path from leaf
    leaf.recomputeUpwards(n, lambda, epsilon);

    return leaf;
  }

  // replace the point at this leaf
  public void replace(int key, float[] point, int n, float lambda, float epsilon) {
    this.key = key;
    this.point = point;

    // recompute with the new point
    recomputeUpwards(n, lambda, epsilon);
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
  public void recomputeUpwards(int n, float lambda, float epsilon) {
    if (parent != null)
      parent.recomputeUpwards(n, lambda, epsilon);
  }

  // return the key at this leaf
  public int key() {
    return key;
  }

  // returns the point at this leaf
  public float[] point() {
    return point;
  }

  // PRINTING FOR DEBUGGING
  public void print() {
    System.out.println(key);
  }
}
