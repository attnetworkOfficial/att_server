package org.attnetwork.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
  public static byte[] sha256(byte[]... data) {
    return _hash("256", data);
  }

  public static byte[] sha512(byte[]... data) {
    return _hash("512", data);
  }

  private static byte[] _hash(String bitLen, byte[]... data) {
    MessageDigest messageDigest = newShaDigest(bitLen);
    for (byte[] datum : data) {
      messageDigest.update(datum);
    }
    return messageDigest.digest();
  }

  private static MessageDigest newShaDigest(String bitLen) {
    try {
      return MessageDigest.getInstance("SHA-" + bitLen);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e.getMessage(), e);  // Can't happen.
    }
  }
}
