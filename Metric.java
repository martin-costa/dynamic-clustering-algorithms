import java.lang.Math;

// implementation of metrics defined on R^n
abstract class Metric {

  // computes the distance between two points with respect to this metric
  public abstract float d(float[] x, float[] y);

}

// implementation of Lp-norm
class LpNorm extends Metric {

  private int p;

  LpNorm(int p) {
    this.p = p;
  }

  public float d(float[] x, float[] y) {

    // ensure vectors have same dimension
    if (x.length != y.length) {
      return 0;
    }

    double sum = 0;
    for (int i = 0; i < x.length; i++) {
      sum += Math.pow(Math.abs(x[i] - y[i]), p);
    }

    return (float)Math.pow(sum, 1.0/(double)p);
  }
}
