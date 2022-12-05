import java.util.*;
import java.io.*;
import java.lang.*;
//import javafx.util.*;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

public class Test2 {

  // 'census' input path
  private static String census = "../data/census_data";

  // 'song' input path
  private static String song = "../data/song";

  // 'kddcup' input path
  private static String kddcup = "../data/kddcup";

  // main function to run tests
  public static void main(String[] args) throws IOException, InterruptedException {

    // parameter k
    int k = 15;

    // the metric to be used
    Metric metric = new LpNorm(1);

    HenzingerTree T = new HenzingerTree(k, metric, 1);

    Random rng = new Random();

    int n = 10;

    for (int i = 0; i < n; i++) {

      float[] a = new float[10];
      for (int j = 0; j < 10; j++) {
        a[j] = rng.nextFloat();
      }

      T.insert(i, a);

      T.print();
      System.out.println("");
    }

    for (int i = 0; i < n; i++) {
      //System.out.println(i);
      T.delete(i);
      T.print();
      System.out.println("");
    }
  }
}
