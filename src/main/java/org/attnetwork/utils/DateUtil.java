package org.attnetwork.utils;

import java.time.Instant;

public class DateUtil {
  public static String toHumanString(Long timestamp) {
    return timestamp == null ? "null" : Instant.ofEpochMilli(timestamp).toString();
  }
}
