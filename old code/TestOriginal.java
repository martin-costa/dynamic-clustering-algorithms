import java.util.*;
import java.io.*;
import java.lang.*;
//import javafx.util.*;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class Test {

  // 'census' input path
  private static String census = "census_data";

  // 'song' input path
  private static String song = "song";

  // 'kddcup' input path
  private static String kddcup = "kddcup";

  private CoresetBFL L;

  // main function to run tests
  public static void main(String[] args) throws IOException, InterruptedException {

    // dataset
    String dataset = args[0];

    // parameter k
    int k = Integer.parseInt(args[1]);

    // create un update stream of length n
    int n = 10000;

    // set the window length
    int windowLength = 2000;

    if (args.length > 2)
      windowLength = Integer.parseInt(args[2]);

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

    // the dynamic algorithms
    DynamicMP dynamicMP = new DynamicMP(k, metric, 30.0f, 0.85f, 0.2f);

    // sampling threshold for the static coreset
    int threshold = 1000;

    HenzingerTree henzingerTree = new HenzingerTree(k, metric, 1.0f);

    runTest(updateStream, dynamicMP, henzingerTree, metric, k, dataset, queryCount);
  }

  // test and compare the dynamic algorithms
  public static void runTest(SlidingWindow updateStream, DynamicMP dynamicMP, HenzingerTree henzingerTree, Metric metric, int k, String dataset, int queryCount) throws IOException {

    // query frequency
    int queryFrequency = (int)(updateStream.streamLength()/queryCount);

    // measures our upadate and query time and Henzinger update time (in nano seconds)
    long updateTimeMP = 0;
    long updateTimeHK = 0;

    long queryTimeMP = 0;
    long queryTimeHK = 0;

    // cost of solution
    float dynamicMPCost = 0;
    float henzingerCost = 0;
    float staticMPCost = 0;
    float kmeansppCost = 0;

    // maintain the current instance in this BBT
    TreeMap<Integer, float[]> activePoints = new TreeMap<Integer, float[]>();

    OnlineKMedian onlineKMedian = new OnlineKMedian(k, metric);
    KMeansPlusPlus kmeanspp = new KMeansPlusPlus(k, metric);

    // create an output streams to write data into file
    DataOutputStream BCLPupdatetimeWriter = new DataOutputStream(new FileOutputStream("results/" + dataset + "_BCLP_updatetime_" + Integer.toString(k)));
    DataOutputStream HK20updatetimeWriter = new DataOutputStream(new FileOutputStream("results/" + dataset + "_HK20_updatetime_" + Integer.toString(k)));

    DataOutputStream BCLPquerytimeWriter = new DataOutputStream(new FileOutputStream("results/" + dataset + "_BCLP_querytime_" + Integer.toString(k)));
    DataOutputStream HK20querytimeWriter = new DataOutputStream(new FileOutputStream("results/" + dataset + "_HK20_querytime_" + Integer.toString(k)));

    DataOutputStream BCLPcostWriter = new DataOutputStream(new FileOutputStream("results/" + dataset + "_BCLP_cost_" + Integer.toString(k)));
    DataOutputStream HK20costWriter = new DataOutputStream(new FileOutputStream("results/" + dataset + "_HK20_cost_" + Integer.toString(k)));
    // DataOutputStream MP03costWriter = new DataOutputStream(new FileOutputStream("results/" + dataset + "_MP03_cost_" + Integer.toString(k)));
    DataOutputStream kmeansppcostWriter = new DataOutputStream(new FileOutputStream("results/" + dataset + "_kmeanspp_cost_" + Integer.toString(k)));

    for (int i = 0; i < updateStream.streamLength(); i++) {

      // if we have an insertion
      if (updateStream.updateType(i)) {

        // run and time the updates for each
        long s = System.nanoTime();
        dynamicMP.insert(updateStream.key(i), updateStream.point(i));
        updateTimeMP += System.nanoTime() - s;

        s = System.nanoTime();
        henzingerTree.insert(updateStream.key(i), updateStream.point(i));
        updateTimeHK += System.nanoTime() - s;

        // add the point to the collection of active points
        activePoints.put(updateStream.key(i), updateStream.point(i));
      }

      // if we have a deletion
      else {

        // run and time the updates for each
        long s = System.nanoTime();
        dynamicMP.delete(updateStream.key(i));
        updateTimeMP += System.nanoTime() - s;

        s = System.nanoTime();
        henzingerTree.delete(updateStream.key(i));
        updateTimeHK += System.nanoTime() - s;

        // add the point to the collection of active points
        activePoints.remove(updateStream.key(i));
      }

      // perform queries
      if (i % queryFrequency == 0) {

        long s = System.nanoTime();
        TreeMap<Integer, Integer> dynamicMPSolution = dynamicMP.cluster();
        queryTimeMP += System.nanoTime() - s;
        dynamicMPCost = cost(activePoints, dynamicMPSolution, metric);

        s = System.nanoTime();
        TreeMap<Integer, Integer> henzingerSolution = henzingerTree.cluster();
        queryTimeHK += System.nanoTime() - s;
        henzingerCost = cost(activePoints, henzingerSolution, metric);

        // run kmeans++
        kmeansppCost = cost(activePoints, kmeanspp.clusterUniform(activePoints), metric);
      }

      // write to files
      BCLPupdatetimeWriter.writeChars(Long.toString(updateTimeMP) + "#");
      HK20updatetimeWriter.writeChars(Long.toString(updateTimeHK) + "#");

      if (i % queryFrequency == 0) {
        BCLPquerytimeWriter.writeChars(Long.toString(queryTimeMP) + "#");
        HK20querytimeWriter.writeChars(Long.toString(queryTimeHK) + "#");
        BCLPcostWriter.writeChars(Float.toString(dynamicMPCost) + "#");
        HK20costWriter.writeChars(Float.toString(henzingerCost) + "#");
        kmeansppcostWriter.writeChars(Float.toString(kmeansppCost) + "#");
      }

      // print
      // System.out.println("------------\n");
      System.out.print("n = ");
      System.out.println(i);
    }

    // close the output streams
    BCLPupdatetimeWriter.close();
    HK20updatetimeWriter.close();
    BCLPquerytimeWriter.close();
    HK20querytimeWriter.close();
    BCLPcostWriter.close();
    HK20costWriter.close();
    kmeansppcostWriter.close();
  }

  // run tests on many algorithmss
  public static void runTests(SlidingWindow updateStream, DynamicAlgorithm[] dynamicAlgorithms, String[] names, Metric metric, String dataset, int queryCount) throws IOException {

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
      updateTimeWriters[i] = new DataOutputStream(new FileOutputStream("results/" + dataset + "_" + names[i] + "_updatetime"));
      queryTimeWriters[i] = new DataOutputStream(new FileOutputStream("results/" + dataset + "_" + names[i] + "_querytime"));
      costWriters[i] = new DataOutputStream(new FileOutputStream("results/" + dataset + "_" + names[i] + "_cost"));
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
      for (int j = 0; j < l; i++) {
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
