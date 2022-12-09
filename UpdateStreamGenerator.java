import java.util.*;
import java.io.*;
import java.lang.Math;

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

  SlidingWindow(int n, int windowLength, String path, int offset) {

    this.n = n;
    this.windowLength = windowLength;

    // load the data
    this.loadData(path, offset);
  }

  SlidingWindow(int n, int windowLength, String path) {

    this.n = n;
    this.windowLength = windowLength;

    // load the data
    this.loadData(path, 0);
  }

  // returns the data point in update i
  public float[] point(int i) {
    return this.data[this.key(i)];
  }

  // returns the (unique) key corresponding to the data point in update i
  public int key(int i) {

    if (i < this.windowLength)
      return i;

    if (i > this.windowLength - 1 + 2*(this.n - this.windowLength))
      return i - this.n;

    if ((i - this.windowLength) % 2 == 1)
      return (i + this.windowLength - 1)/2;

    return (i - this.windowLength)/2;
  }

  // returns whether update i is an insertion (true) / deletion (false)
  public boolean updateType(int i) {

    if (i < this.windowLength)
      return true;

    if (i > this.windowLength - 1 + 2*(this.n - this.windowLength))
      return false;

    if ((i - this.windowLength) % 2 == 1)
      return true;

    return false;
  }

  // loads the data points
  private void loadData(String path, int offset) {

    Scanner scanner = null;

    // create scanner to read from file
    try {
      scanner = new Scanner(new File(path));
    } catch(FileNotFoundException error) {}

    this.n = Math.min(scanner.nextInt(), this.n - offset);
    this.d = scanner.nextInt();

    // ensure window is not too large
    this.windowLength = Math.min(n, windowLength);

    // initialise array to store data
    data = new float[this.n][this.d];

    // move past first offset many points
    for (int i = 0; i < offset*d; i++) {
      scanner.nextFloat();
    }

    // load the data from the file
    for (int i = 0; i < this.n; i++) {
      for (int j = 0; j < this.d; j++) {
        data[i][j] = scanner.nextFloat();
      }
    }

    // close the scanner
    scanner.close();
  }

  // check if there is an update i
  public int streamLength() {
    return this.n*2;
  }
}
