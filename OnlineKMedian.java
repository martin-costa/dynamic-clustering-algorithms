import java.util.*;
import java.io.*;

/*

implementation of (static) local search of Arya et al.

*/

public class OnlineKMedian {

  // parameter for k clustering
  private int k;

  // metric used to evaluate distances
  private Metric metric;

  // parameters for the algorithm
  private float alpha;

  private float beta;

  private float gamma;

  // the points defining the instance
  private TreeMap<Integer, float[]> points;

  // the weights of the points
  private TreeMap<Integer, Float> weights;

  // the number of points in the space
  private int n;

  // auxiliary data structures used for solving the problem

  // an array of the keys corresponding to the points in the input
  private Integer[] keysArr;

  // an array of the points
  private float[][] pointsArr;

  // an array of the weights
  private float[] weightsArr;

  // an array containing medians found so far
  private int[] medians;

  // a BBT of the medians found so far
  private PriorityQueue<Integer> nonMedians;

  // keep track of medians found so far
  private int found = 0;

  // the following are used for finding the values of balls

  // distance from each point to the closest median
  private float[] distFromMedians;

  // arrays of the points sorted by their distances
  private int[][] sortedPointsArr;

  // sortedPointsDistArr[i][j] = d(pointArr[i], pointsArr[sortedPointsArr[i][j]])
  private float[][] sortedPointsDistArr;

  // arrays to make it easier to find the values of balls
  private float[][] ballValueAux1;
  private float[][] ballValueAux2;

  // define the parameters
  public OnlineKMedian(int k, Metric metric) {

    this.k = k;
    this.metric = metric;

    this.alpha = 2 + (float)Math.sqrt(3);
    this.beta = (this.alpha - 1)/(this.alpha - 2);
    this.gamma = this.alpha*(1 + this.alpha)/(this.alpha - 2);
  }

  // given an unweighted metric space, find a k clustering of the points
  public TreeMap<Integer, Integer> cluster(TreeMap<Integer, float[]> points) {

    // set all the weights to be 1
    TreeMap<Integer, Float> weights = new TreeMap<Integer, Float>();

    Set<Integer> keySet = points.keySet();

    for (Integer key : keySet) {
      weights.put(key, 1.0f);
    }

    // cluster this instance
    return cluster(points, weights);
  }

  // given a weighted metric space, find a k clustering of the points
  public TreeMap<Integer, Integer> cluster(TreeMap<Integer, float[]> points, TreeMap<Integer, Float> weights) {

    // save the input
    this.points = points;
    this.weights = weights;

    // the number of points in the input
    this.n = this.points.size();

    // create the auxiliary data structures
    setUpDataStructures();

    return cluster();
  }

  // implementation of online k-median algorithm
  public TreeMap<Integer, Integer> cluster() {

    // implementation of online k-median algorithm
    while (found < k) {

      // find the next median
      findNextMedian();
      found++;
    }

    return null;
  }

  // find the next median
  private void findNextMedian() {

  }

  // return the max value of isolated(x, medians) for some x not in medians
  private Pair maxValueIsolated() {

    // keep track of ball of best value
    Pair ball = new Pair(-1, 0.0f);

    // the value of ball
    float bestValue = 0;

    for (Integer j : nonMedians) {

      float r = isolated(j);

      float value = ballValue(j, r);

      if (value >= bestValue) {
        bestValue = value;
        ball = new Pair(j, r);
      }
    }

    return ball;
  }

  // return the value of isolated(x, medians) for some x not in medians
  private float isolated(int i) {

    // if there are no medians, return distance of furthest point
    if (found == 0)
      return sortedPointsDistArr[i][n-1];

    return distFromMedians[i]/gamma;
  }

  // find the value of the ball with center
  private float ballValue(int i, float r) {

    // find the furthest point from point i that is not more than r away
    int j = Arrays.binarySearch(sortedPointsDistArr[i], r);

    if (i < 0) i = -i-2;

    return r*ballValueAux1[i][j] - ballValueAux2[i][j];
  }

  private void setUpDataStructures() {

    // end if the input is empty
    if (n <= 0) return;

    // place the keys of the points into an array
    keysArr = points.keySet().toArray(new Integer[0]);

    // the dimension of the data
    int d = points.get(keysArr[0]).length;

    // create an array to store the points
    pointsArr = new float[n][d];

    // create an array to store the weights
    weightsArr = new float[n];

    for (int i = 0; i < n; i++) {
      pointsArr[i] = points.get(keysArr[i]);
      weightsArr[i] = weights.get(keysArr[i]);
    }

    // initialise array to store medians
    medians = new int[k];

    // initialise PriorityQueue for searching non median points
    nonMedians = new PriorityQueue<Integer>();

    for (int i = 0; i < n; i++) {
      nonMedians.add(i);
    }

    // store distances of points from medians
    distFromMedians = new float[n];
    Arrays.fill(distFromMedians, Float.POSITIVE_INFINITY);

    // create a 2D array of points sorted by distance from each other
    sortedPointsArr = new int[n][n];
    sortedPointsDistArr = new float[n][n];

    Pair[] sortingArr = new Pair[n];

    for (int i = 0; i < n; i++) {

      // place the indices of the points and their distances from i into an array
      for (int j = 0; j < n; j++) {
        sortingArr[j] = new Pair(j, metric.d(pointsArr[i], pointsArr[j]));
      }

      Arrays.sort(sortingArr);

      for (int j = 0; j < n; j++) {
        Pair p = sortingArr[j];
        sortedPointsArr[i][j] = p.l;
        sortedPointsDistArr[i][j] = p.r;
      }
    }

    ballValueAux1 = new float[n][n];
    ballValueAux2 = new float[n][n];

    // compute the auxiliary information to enable fast ball value computations
    for (int i = 0; i < n; i++) {
      ballValueAux1[i][0] = weightsArr[sortedPointsArr[i][0]];
      ballValueAux2[i][0] = sortedPointsDistArr[i][0]*weightsArr[sortedPointsArr[i][0]];
      for (int j = 1; j < n; j++) {
        ballValueAux1[i][j] = ballValueAux1[i][j-1] + weightsArr[sortedPointsArr[i][j]];
        ballValueAux2[i][j] = ballValueAux2[i][j-1] + sortedPointsDistArr[i][j]*weightsArr[sortedPointsArr[i][j]];
      }
    }

    // System.out.println(Arrays.deepToString(sortedPointsArr));
    // System.out.println(Arrays.deepToString(sortedPointsDistArr));
  }

}
