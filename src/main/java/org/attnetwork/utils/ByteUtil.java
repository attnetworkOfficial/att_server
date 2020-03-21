package org.attnetwork.utils;

public class ByteUtil {

  public static byte[] concat(byte[]... bytes) {
    int l = 0;
    for (byte[] b : bytes) {
      if (b != null) {
        l += b.length;
      }
    }
    byte[] result = new byte[l];
    int i = 0;
    for (byte[] b : bytes) {
      if (b != null) {
        System.arraycopy(b, 0, result, i, b.length);
        i += b.length;
      }
    }
    return result;
  }

  public static byte[] subArray(byte[] src, int start, int len) {
    byte[] sub = new byte[len];
    System.arraycopy(src, start, sub, 0, len);
    return sub;
  }
}
