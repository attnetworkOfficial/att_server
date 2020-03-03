package org.attnetwork.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {

  public static byte[] sha256(byte[] data) {
    return _hash(data, "256");
  }

  public static byte[] sha512(byte[] data) {
    return _hash(data, "512");
  }

  private static byte[] _hash(byte[] data, String b) {
    return newShaDigest(b).digest(data);
  }

  private static MessageDigest newShaDigest(String b) {
    try {
      return MessageDigest.getInstance("SHA-" + b);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e.getMessage(), e);  // Can't happen.
    }
  }
}
