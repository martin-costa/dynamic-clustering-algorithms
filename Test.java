import java.util.*;
import java.io.*;
import java.lang.*;
//import javafx.util.*;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class Test {

  // 'census' input path
  private static String census = "../data/census_data";

  // 'song' input path
  private static String song = "../data/song";

  // 'kddcup' input path
  private static String kddcup = "../data/kddcup";

  // main function to run tests
  public static void main(String[] args) throws IOException, InterruptedException {

    // parameter k
    int k = 25;

    // the metric to be used
    Metric metric = new LpNorm(1);

    // our dynamic algorithm
    DynamicMP dynamicMP = new DynamicMP(k, metric, 5.0f, 0.75f, 0.2f);

    // the state of the art Henzinger Kale data structure
    HenzingerTree henzingerTree = new HenzingerTree(k, metric, 1.0f);

    // create un update stream of length n
    int n = 2000;
    int windowLength = 1000;

    SlidingWindow updateStream = new SlidingWindow(n, windowLength, census);

    // create a static algorithm
    OnlineKMedian staticKMedian = new OnlineKMedian(k, metric);

    // kmeansplusplus static algorithm
    KMeansPlusPlus kmeanspp = new KMeansPlusPlus(k, metric);

    // maintain the current instance in this BBT
    TreeMap<Integer, float[]> activePoints = new TreeMap<Integer, float[]>();

    // run the coreset on this update stream
    for (int i = 0; i < updateStream.streamLength(); i++) {

      // output metrics
      //dynamicMP.printStats();

      // if we have an insertion
      if (updateStream.updateType(i)) {
        dynamicMP.insert(updateStream.key(i), updateStream.point(i));
        //henzingerTree.insert(updateStream.key(i), updateStream.point(i));
        activePoints.put(updateStream.key(i), updateStream.point(i));
      }

      // if we have a deletion
      else {
        dynamicMP.delete(updateStream.key(i));
        //henzingerTree.delete(updateStream.key(i));
        activePoints.remove(updateStream.key(i));
      }

      if (i % 25 == 0 && false) {

        // our dynamic algorithm
        TreeMap<Integer, Integer> dynamicMPSolution = dynamicMP.cluster();
        float dynamicMPCost = cost(activePoints, dynamicMPSolution, metric);

        // Henzinger core
        TreeMap<Integer, Integer> henzingerSolution = henzingerTree.cluster();
        float henzingerCost = cost(activePoints, henzingerSolution, metric);

        // static online k median
        // TreeMap<Integer, Integer> staticKMedianSolution = staticKMedian.cluster(activePoints);
        // float staticKMedianCost = cost(activePoints, staticKMedianSolution, metric);
        //System.out.println(staticKMedianCost);

        // kmeansplusplus
        // TreeMap<Integer, Integer> kmeansppSolution = kmeanspp.cluster(activePoints);
        // float kmeansppCost = cost(activePoints, kmeansppSolution, metric);
        //System.out.println(kmeansppCost);

        // print
        System.out.println(dynamicMPCost);
        System.out.println(henzingerCost);

        System.out.println(i);
        System.out.println("");
      }
      else {
        System.out.println(i);
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
