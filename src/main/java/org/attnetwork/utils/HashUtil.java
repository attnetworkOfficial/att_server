package org.attnetwork.utils;

import java.security.MessageDigest;
import java.security.Security;
import java.util.HashMap;
import org.attnetwork.exception.AException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class HashUtil {

  public static final HashUtil SHA3_256 = new HashUtil("SHA3-256");
  public static final HashUtil SHA3_512 = new HashUtil("SHA3-512");

  public static final HashMap<String, HashUtil> map = new HashMap<>();

  static {
    Security.addProvider(new BouncyCastleProvider());
    map.put("SHA3-256", SHA3_256);
    map.put("SHA3-512", SHA3_512);
  }

  public static byte[] hash(String algorithm, byte[]... data) {
    HashUtil hashUtil = map.get(algorithm);
    if (hashUtil == null) {
      throw new AException("unsupported hash algorithm: " + algorithm);
    }
    return hashUtil.hash(data);
  }

  private final String algorithm;

  private HashUtil(String algorithm) {
    this.algorithm = algorithm;
  }

  public byte[] hash(byte[]... data) {
    MessageDigest messageDigest = newShaDigest(algorithm);
    for (byte[] datum : data) {
      messageDigest.update(datum);
    }
    return messageDigest.digest();
  }

  private static MessageDigest newShaDigest(String algorithm) {
    try {
      return MessageDigest.getInstance(algorithm, "BC");
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);  // Can't happen.
    }
  }
}
