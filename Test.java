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
    int k = 20;

    DynamicMP coreset = new DynamicMP(k, new LpNorm(1), 1.0f, 0.5f, 0.1f);

    // create un update stream of length n
    int n = 2000;
    int windowLength = 2000;

    SlidingWindow updateStream = new SlidingWindow(n, windowLength, census);

    // create a static algorithm
    OnlineKMedian staticKMedian = new OnlineKMedian(k, new LpNorm(1));

    // maintain the current instance in this BBT
    TreeMap<Integer, float[]> activePoints = new TreeMap<Integer, float[]>();

    // run the coreset on this update stream
    for (int i = 0; i < updateStream.streamLength(); i++) {

      // output metrics
      //coreset.printStats();

      // if we have an insertion
      if (updateStream.updateType(i)) {
        coreset.insert(updateStream.key(i), updateStream.point(i));
        activePoints.put(updateStream.key(i), updateStream.point(i));
      }

      // if we have a deletion
      else {
        coreset.delete(updateStream.key(i));
        activePoints.remove(updateStream.key(i));
      }

      if (i % 100 == 0) {

        staticKMedian.cluster(activePoints);

        //System.out.println(staticKMedian.cost());

        System.out.println(coreset.cluster() / staticKMedian.cost());

        //System.out.println(coreset.cluster());

        System.out.println(i);

        System.out.println("");

      }


    }

    // treemap time: 98957, 99489
    // hashmap time: 102460, 103128
  }
}
