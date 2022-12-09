import java.util.*;
import java.io.*;
import java.lang.*;
//import javafx.util.*;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class Test {

  // 'census' input path
  private static String census = "census";

  // 'song' input path
  private static String song = "song";

  // 'kddcup' input path
  private static String kddcup = "kddcup";

  private CoresetBFL compilingBFL;

  // main function to run tests
  public static void main(String[] args) throws IOException, InterruptedException {

    /*

    set parameters for the tests

    */

    // dataset
    String dataset = census;
    if (args.length > 0)
      dataset = args[0];

    // parameter k
    int k = 10;
    if (args.length > 1)
      k = Integer.parseInt(args[1]);

    // set the window length
    int windowLength = 2000;
    if (args.length > 2)
      windowLength = Integer.parseInt(args[2]);

    // create un update stream of length n
    int n = 10000;
    if (args.length > 3)
      n = Integer.parseInt(args[3]);

    // number of queries to perform over the stream
    int queryCount = (int)(n / 10);
    if (args.length > 4)
      queryCount = Integer.parseInt(args[4]);

    // the metric to be used
    Metric metric = new LpNorm(1);

    // create update stream
    SlidingWindow updateStream = new SlidingWindow(n, windowLength, "../data/" + dataset);

    /*

    run the tests

    */

    float beta = 0.5f;
    float epsilon = 0.2f;

    DynamicAlgorithm[] dynamicAlgorithms = new DynamicAlgorithm[6];

    dynamicAlgorithms[0] = new DynamicMP(k, metric, 15.0f, beta, epsilon);
    dynamicAlgorithms[1] = new DynamicMP(k, metric, 30.0f, beta, epsilon);
    dynamicAlgorithms[2] = new DynamicMP(k, metric, 60.0f, beta, epsilon);

    dynamicAlgorithms[3] = new HenzingerTree(k, metric, 250);
    dynamicAlgorithms[4] = new HenzingerTree(k, metric, 500);
    dynamicAlgorithms[5] = new HenzingerTree(k, metric, 1000);

    //runTests(updateStream, dynamicAlgorithms, metric, dataset, queryCount);

    int[] kValues = { 10, 50, 100 };
    float[] alphaValues = { 15.0f, 30.0f, 60.0f };
    int[] mValues = { 250, 500, 1000 };

    runBatchTests(kValues, alphaValues, mValues);
  }

  // run tests on many algorithmss
  public static void runTests(SlidingWindow updateStream, DynamicAlgorithm[] dynamicAlgorithms, Metric metric, String dataset, int queryCount) throws IOException {

    // query frequency
    int queryFrequency = (int)(updateStream.streamLength()/queryCount);

    // the number of algorithms we are testing
    int l = dynamicAlgorithms.length;

    // measures the update times
    long[] updateTimes = new long[l];

    // measures the query times
    long[] queryTimes = new long[l];

    // measures the costs
    float[] costs = new float[l];

    // maintain the current instance in this BBT
    TreeMap<Integer, float[]> activePoints = new TreeMap<Integer, float[]>();

    // create output streams to write to files
    DataOutputStream[] updateTimeWriters = new DataOutputStream[l];
    DataOutputStream[] queryTimeWriters = new DataOutputStream[l];
    DataOutputStream[] costWriters = new DataOutputStream[l];

    for (int i = 0; i < l; i++) {
      updateTimeWriters[i] = new DataOutputStream(new FileOutputStream("results/" + dataset + "_" + dynamicAlgorithms[i].name() + "_updatetime"));
      queryTimeWriters[i] = new DataOutputStream(new FileOutputStream("results/" + dataset + "_" + dynamicAlgorithms[i].name() + "_querytime"));
      costWriters[i] = new DataOutputStream(new FileOutputStream("results/" + dataset + "_" + dynamicAlgorithms[i].name() + "_cost"));
    }

    // handle the update stream
    for (int i = 0; i < updateStream.streamLength(); i++) {

      // update the active points
      if (updateStream.updateType(i))
        activePoints.put(updateStream.key(i), updateStream.point(i));
      if (!updateStream.updateType(i))
        activePoints.remove(updateStream.key(i));

      // handle and time the update for each one of the algorithms
      for (int j = 0; j < l; j++) {

        long s = System.nanoTime();

        if (updateStream.updateType(i))
          dynamicAlgorithms[j].insert(updateStream.key(i), updateStream.point(i));
        if (!updateStream.updateType(i))
          dynamicAlgorithms[j].delete(updateStream.key(i));

        updateTimes[j] += System.nanoTime() - s;
      }

      // perform query every queryFrequency updates
      if (i % queryFrequency == 0) {

        for (int j = 0; j < l; j++) {

          long s = System.nanoTime();

          // cluster and find the solution
          TreeMap<Integer, Integer> solution = dynamicAlgorithms[j].cluster();

          queryTimes[j] += System.nanoTime() - s;

          // find the cost
          costs[j] = cost(activePoints, solution, metric);
        }
      }

      // write to files
      for (int j = 0; j < l; j++) {
        updateTimeWriters[j].writeChars(Long.toString(updateTimes[j]) + "#");
      }

      if (i % queryFrequency == 0) {
        for (int j = 0; j < l; j++) {
          queryTimeWriters[j].writeChars(Long.toString(queryTimes[j]) + "#");
          costWriters[j].writeChars(Float.toString(costs[j]) + "#");
        }
      }

      // print
      System.out.print("n = ");
      System.out.println(i);
    }

    // close the file streams
    for (int i = 0; i < l; i++) {
      updateTimeWriters[i].close();
      queryTimeWriters[i].close();
      costWriters[i].close();
    }
  }

  // run the complete tests
  public static void runBatchTests(int[] kValues, float[] alphaValues, int[] mValues) throws IOException {

    int n = 10000;
    int windowLength = 2000;
    int queryCount = 1000;

    Metric metric = new LpNorm(1);

    float beta = 0.5f;
    float epsilon = 0.2f;

    String[] datasets = {census, song, kddcup};

    for (int k : kValues) {
      for (String dataset : datasets) {

        DynamicAlgorithm[] dynamicAlgorithms = new DynamicAlgorithm[6];

        dynamicAlgorithms[0] = new DynamicMP(k, metric, 15.0f, beta, epsilon);
        dynamicAlgorithms[1] = new DynamicMP(k, metric, 30.0f, beta, epsilon);
        dynamicAlgorithms[2] = new DynamicMP(k, metric, 60.0f, beta, epsilon);

        dynamicAlgorithms[3] = new HenzingerTree(k, metric, 250);
        dynamicAlgorithms[4] = new HenzingerTree(k, metric, 500);
        dynamicAlgorithms[5] = new HenzingerTree(k, metric, 1000);

        SlidingWindow updateStream = new SlidingWindow(n, windowLength, "../data/" + dataset);

        runTests(updateStream, dynamicAlgorithms, metric, dataset, queryCount);
      }
    }
  }

  // compute the cost of solution with respect points with this metric
  public static float cost(TreeMap<Integer, float[]> points, TreeMap<Integer, Integer> solution, Metric metric) {

    Integer[] pointsArr = points.keySet().toArray(new Integer[0]);
    Integer[] solutionArr = solution.keySet().toArray(new Integer[0]);

    float cost = 0;

    for (int i = 0; i < pointsArr.length; i++) {
      float dist = Float.POSITIVE_INFINITY;

      for (int j = 0; j < solutionArr.length; j++) {

        float d = metric.d(points.get(pointsArr[i]), points.get(solutionArr[j]));
        if (d <= dist) dist = d;
      }

      cost += dist;
    }

    return cost;
  }
}
