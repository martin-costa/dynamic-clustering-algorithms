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

  // main function to run tests
  public static void main(String[] args) throws IOException, InterruptedException {

    // dataset
    String dataset = args[0];

    // parameter k
    int k = Integer.parseInt(args[1]);

    // create un update stream of length n
    int n = 10000;

    // set the window length
    int windowLength = 1000;

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
    DynamicMP dynamicMP = new DynamicMP(k, metric, 15.0f, 0.85f, 0.2f);
    HenzingerTree henzingerTree = new HenzingerTree(k, metric, 1.0f);

    runTest(updateStream, dynamicMP, henzingerTree, metric, k, dataset, queryCount, true, true);
  }

  // test and compare the dynamic algorithms
  public static void runTest(SlidingWindow updateStream, DynamicMP dynamicMP, HenzingerTree henzingerTree, Metric metric, int k, String dataset, int queryCount, boolean runMP, boolean runHK) throws IOException {

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
        if (runMP) {
          long s = System.nanoTime();
          dynamicMP.insert(updateStream.key(i), updateStream.point(i));
          updateTimeMP += System.nanoTime() - s;
        }
        if (runHK) {
          long s = System.nanoTime();
          henzingerTree.insert(updateStream.key(i), updateStream.point(i));
          updateTimeHK += System.nanoTime() - s;
        }

        // add the point to the collection of active points
        activePoints.put(updateStream.key(i), updateStream.point(i));
      }

      // if we have a deletion
      else {

        // run and time the updates for each
        if (runMP) {
          long s = System.nanoTime();
          dynamicMP.delete(updateStream.key(i));
          updateTimeMP += System.nanoTime() - s;
        }
        if (runHK) {
          long s = System.nanoTime();
          henzingerTree.delete(updateStream.key(i));
          updateTimeHK += System.nanoTime() - s;
        }

        // add the point to the collection of active points
        activePoints.remove(updateStream.key(i));
      }

      // perform queries
      if (i % queryFrequency == 0) {
        if (runMP) {
          long s = System.nanoTime();
          TreeMap<Integer, Integer> dynamicMPSolution = dynamicMP.cluster();
          queryTimeMP += System.nanoTime() - s;
          dynamicMPCost = cost(activePoints, dynamicMPSolution, metric);
        }
        if (runHK) {
          long s = System.nanoTime();
          TreeMap<Integer, Integer> henzingerSolution = henzingerTree.cluster();
          queryTimeHK += System.nanoTime() - s;
          henzingerCost = cost(activePoints, henzingerSolution, metric);
        }

        // // run static online k median
        // staticMPCost = cost(activePoints, onlineKMedian.cluster(activePoints), metric);

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
      // System.out.print("n = ");
      // System.out.print(i);
      // System.out.println("");
      // System.out.print("MP update time = ");
      // System.out.println(updateTimeMP*0.000000001);
      // System.out.print("HK update time = ");
      // System.out.println(updateTimeHK*0.000000001);
      // System.out.println("");
      // if (i % queryFrequency == 0) {
      //   System.out.print("MP cost = ");
      //   System.out.println(dynamicMPCost);
      //   System.out.print("HK cost = ");
      //   System.out.println(henzingerCost);
      //   System.out.print("kmeans++ cost = ");
      //   System.out.println(kmeansppCost);
      // }
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
