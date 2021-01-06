package org.attnetwork.utils;


import org.attnetwork.exception.AException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class GoogleAuth {
  private static final String QR_FORMAT = "otpauth://totp/%s?secret=%s&issuer=%s";

  public static String genSecret(int secretSize) {
    return Base32.encode(RandomUtil.randomBytes(secretSize));
  }

  public static String getQRBarcodeStr(String account, String secret, String issuer) {
    return String.format(QR_FORMAT, account, secret, issuer);
  }

  public static boolean verify(String secret, int code, int left, int right) {
    return verify(Base32.decode(secret), code, left, right);
  }

  public static boolean verify(byte[] secret, int code, int left, int right) {
    long t = System.currentTimeMillis() / 30000L;
    for (int i = -left; i <= right; ++i) {
      if (calCode(secret, t + (long) i) == code) {
        return true;
      }
    }
    return false;
  }

  private static int calCode(byte[] key, long t) {
    try {
      byte[] data = new byte[8];
      for (int i = 8; i-- > 0; t >>>= 8) {
        data[i] = (byte) t;
      }
      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(new SecretKeySpec(key, "HmacSHA1"));
      byte[] hash = mac.doFinal(data);
      int offset = hash[20 - 1] & 0xF;
      long truncatedHash = 0;
      for (int i = 0; i < 4; ++i) {
        truncatedHash <<= 8;
        truncatedHash |= (hash[offset + i] & 0xFF);
      }
      truncatedHash &= 0x7FFFFFFF;
      truncatedHash %= 1000000;
      return (int) truncatedHash;
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      throw new AException(e);
    }
  }
}
