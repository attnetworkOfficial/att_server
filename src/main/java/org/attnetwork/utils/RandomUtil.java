package org.attnetwork.utils;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Random;

public class RandomUtil {
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();
  private static final Random RANDOM = new Random();

  public static byte[] randomBytes(int len) {
    byte[] bytes = new byte[len];
    SECURE_RANDOM.nextBytes(bytes);
    return bytes;
  }

  public static boolean randomBoolean() {
    return RANDOM.nextBoolean();
  }

  public static BigDecimal randomBetween(BigDecimal a, BigDecimal b, int scale) {
    BigDecimal v = b.subtract(a).stripTrailingZeros();
    if (scale != 0) {
      v = v.movePointRight(scale);
    }
    v = v.multiply(BigDecimal.valueOf(RANDOM.nextDouble()));
    if (scale != 0)
      v = v.movePointLeft(scale);
    return a.add(v).setScale(scale, BigDecimal.ROUND_HALF_EVEN);
  }

  public static BigDecimal randomBetweenNotContain(BigDecimal a, BigDecimal b, int scale) {
    BigDecimal v = b.subtract(a).stripTrailingZeros();
    if (scale != 0) {
      v = v.movePointRight(scale).subtract(BigDecimal.valueOf(2));
    }
    v = v.multiply(BigDecimal.valueOf(RANDOM.nextDouble())).add(BigDecimal.ONE);
    if (scale != 0)
      v = v.movePointLeft(scale);
    return a.add(v).setScale(scale, BigDecimal.ROUND_HALF_EVEN);
  }

  public static BigDecimal randomBetween(BigDecimal a, boolean notContainA, BigDecimal b, boolean notContainB, int scale) {
    BigDecimal v = b.subtract(a).stripTrailingZeros();
    if (scale != 0) {
      v = v.movePointRight(scale);
      if (notContainA) {
        if (notContainB) {
          v = v.subtract(BigDecimal.valueOf(2));
        } else {
          v = v.subtract(BigDecimal.ONE);
        }
      } else if (notContainB)
        v = v.subtract(BigDecimal.ONE);
    }
    v = v.multiply(BigDecimal.valueOf(RANDOM.nextDouble()));
    if (notContainA)
      v = v.add(BigDecimal.ONE);
    if (scale != 0)
      v = v.movePointLeft(scale);
    return a.add(v).setScale(scale, BigDecimal.ROUND_HALF_EVEN);
  }
}
