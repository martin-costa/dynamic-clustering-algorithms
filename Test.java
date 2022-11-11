import java.util.*;
import java.io.*;

import java.nio.ByteBuffer;

public class Test {

  public static void main(String[] args) throws IOException {

    DynamicMP coreset = new DynamicMP(10, new LpNorm(1), 2f, 0.5f, 0.01f);

    // 'song' input path
    String path = "../data/kddcup";

    Scanner scanner = new Scanner(new File(path));

    int n = scanner.nextInt();
    int size = scanner.nextInt();

    float[] feature = new float[size];

    for (int i = 0; i < size; i++) {
      feature[i] = scanner.nextFloat();
    }

    System.out.println(n);
    System.out.println(size);

    System.out.println(Arrays.toString(feature));
    System.out.println(feature[25]);



    // int count = (stream.read() << 24) | (stream.read() << 16) | (stream.read() << 8) | (stream.read());
    //
    // System.out.println(count);
    //
    //
    // byte[] b = new byte[4];
    // stream.read(b);
    //
    // System.out.println(ByteBuffer.wrap(b).getInt());
    //
    //
    // String line = scanner.nextLine();
    //
    // for (int i = 0; i < 10; i++) {
    //   System.out.println(scanner.nextLine());
    // }

  }


}
