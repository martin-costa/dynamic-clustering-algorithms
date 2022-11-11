import java.util.*;
import java.io.*;

// generates a stream of m updates given data path

abstract class UpdateStreamGenerator {

  // returns the data point in update i
  public abstract float[] point(int i);

  // returns the (unique) key corresponding to the data point in update i
  public abstract int key(int i);

  // returns whether update i is an insertion (true) / deletion (false)
  public abstract boolean updateType(int i);

}

// generates update sequences in the sliding window model
class SlidingWindow extends UpdateStreamGenerator {

  // number of points to insert and delete
  private int n;

  // the length of the sliding window
  private int windowLength;

  // the data points
  private float[][] data;

  // stores the dimension of the data
  private int d;

  SlidingWindow(int n, int windowLength, String path) {

    this.n = n;
    this.windowLength = windowLength;

    this.loadData(path);
  }

  // returns the data point in update i
  public float[] point(int i) {

  }

  // returns the (unique) key corresponding to the data point in update i
  public int key(int i) {

  }

  // returns whether update i is an insertion (true) / deletion (false)
  public boolean updateType(int i) {

  }

  // loads the data points
  private void loadData(String path) throws IOException {

    // create scanner to read from file
    Scanner scanner = new Scanner(new File(path));

    this.n = Math.min(scanner.nextInt(), this.n);
    this.d = scanner.nextInt();

    data = new float[this.n][this.d];

    for (int i = 0; i < size; i++) {
      for (int j = 0; j < size; j++) {
        data[i][j] = scanner.nextFloat();
      }
    }
  }

}
