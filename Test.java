import java.util.*;
import java.io.*;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class Test {

  public static void main(String[] args) throws IOException, InterruptedException {

    DynamicMP coreset = new DynamicMP(7, new LpNorm(1), 1.0f, 0.5f, 0.1f);

    // 'census' input path
    String census = "../data/census_data";

    // 'song' input path
    String song = "../data/song";

    // 'kddcup' input path
    String kddcup = "../data/kddcup";

    // create un update stream of length n
    int n = 10000;
    int windowLength = 5000;

    SlidingWindow updateStream = new SlidingWindow(n, windowLength, song);

    // run the coreset on this update stream
    for (int i = 0; i < updateStream.streamLength(); i++) {

      TimeUnit.MILLISECONDS.sleep(1);

      //coreset.printStats();
      System.out.println(i);

      // if we have an insertion
      if (updateStream.updateType(i)) {
        coreset.insert(updateStream.key(i), updateStream.point(i));
      }

      // if we have a deletion
      else {
        coreset.delete(updateStream.key(i));
      }
    }
  }
}
