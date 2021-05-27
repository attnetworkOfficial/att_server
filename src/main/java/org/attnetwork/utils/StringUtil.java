package org.attnetwork.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
  private static final String UNICODE_REGEXP = "\\\\u[0-9a-fA-F]{4}";
  private static final String UNICODE_REGEXP_WITH_GROUP = "\\\\u([0-9a-fA-F]{4})";
  private static final Pattern UNICODE_REGEXP_PATTERN = Pattern.compile(UNICODE_REGEXP_WITH_GROUP);
  private static final Pattern CHINESE_REGEXP_PATTERN = Pattern.compile("[\u4e00-\u9fa5]");
  private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("_([a-zA-Z])");
  private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("([A-Z])");
  private static final Pattern JSON_KEY_PATTERN = Pattern.compile("(\"\\w+\" *:)");

  private static final ObjectMapper mapper = new ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .setSerializationInclusion(JsonInclude.Include.NON_NULL);

  public static <T> String writeValueAsJsonString(T value) {
    try {
      return mapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T readJsonStringAsValue(String str, Class<T> type) {
    try {
      return mapper.readValue(str == null ? "{}" : str, type);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean containsIgnoreCase(String source, String contains) {
    return Pattern.compile(Pattern.quote(contains), Pattern.CASE_INSENSITIVE)
        .matcher(source).find();
  }

  public static boolean stringInStringArray(String strA, String... strs) {
    if (strA == null) {
      return false;
    }
    for (String s : strs) {
      if (strA.equals(s)) {
        return true;
      }
    }
    return false;
  }

  public static String purePhone(String nc, String p) {
    return pureNumber(nc) + "-" + pureNumber(p);
  }

  public static String pureNumber(String s) {
    return s.replaceAll("[^\\d]", "");
  }

  public static String wrapInvertedComma(String s) {
    return wrap(s, "'");
  }

  public static String wrap(String s, String w) {
    return w + s + w;
  }

  public static String maxLenCut(String s, int max) {
    return s == null ? null : s.length() > max ? s.substring(0, max) : s;
  }

  public static String getFileExt(String fileName) {
    if (fileName == null) {
      throw new IllegalArgumentException("文件名不能为空");
    }
    int i = fileName.lastIndexOf(".");
    if (i < 0 || i == fileName.length() - 1) {
      throw new IllegalArgumentException("文件名不合法:" + fileName);
    }
    return fileName.substring(i + 1);
  }

  public static String decodeUnicodeString(String s) {
    Matcher m = UNICODE_REGEXP_PATTERN.matcher(s);
    while (m.find()) {
      int c = Integer.parseInt(m.group(1), 16);
      s = s.replaceFirst(UNICODE_REGEXP, c == 0x1A/*EOI*/ ? "" : String.valueOf((char) c));
      m = UNICODE_REGEXP_PATTERN.matcher(s);
    }
    return s;
  }

  public static String limitLengthReplaceWith3Dot(String s, int limit) {
    if (s.length() <= limit) {
      return s;
    } else if (limit <= 3) {
      return "...";
    }
    return s.substring(0, limit - 3) + "...";
  }

  public static String removeUnicodeString(String s) {
    return s.replaceAll(UNICODE_REGEXP, "");
  }

  public static String copy(String s, int d) {
    StringBuilder sb = new StringBuilder(s);
    for (int i = 0; i < d; i++) {
      sb.append(s);
    }
    return sb.toString();
  }

  public static String fileNameAddPrefix(String prefix, String fileName) {
    String[] ss = fileName.split("/");
    StringBuilder sb = new StringBuilder();
    for (int i = 0, l = ss.length - 1; i < l; i++) {
      sb.append(ss[i]).append("/");
    }
    sb.append(prefix).append(ss[ss.length - 1]);
    return sb.toString();
  }

  public static boolean containsChinese(String str) {
    if (str == null) {
      return false;
    }
    return CHINESE_REGEXP_PATTERN.matcher(str).find();
  }

  public static String camelCase2SnakeCase(String camelString) {
    Matcher m = CAMEL_CASE_PATTERN.matcher(camelString);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      m.appendReplacement(sb, "_" + m.group(1).toLowerCase());
    }
    m.appendTail(sb);
    return sb.toString();
  }

  public static String jsonKeyCamelCase2SnakeCase(String json) {
    Matcher m = JSON_KEY_PATTERN.matcher(json);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      m.appendReplacement(sb, camelCase2SnakeCase(m.group(1)));
    }
    m.appendTail(sb);
    return sb.toString();
  }

  public static String snakeCase2CamelCase(String snakeString) {
    Matcher m = SNAKE_CASE_PATTERN.matcher(snakeString);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      m.appendReplacement(sb, m.group(1).toUpperCase());
    }
    m.appendTail(sb);
    return sb.toString();
  }

  public static String jsonKeySnakeCase2CamelCase(String json) {
    Matcher m = JSON_KEY_PATTERN.matcher(json);
    StringBuffer sb = new StringBuffer();
    while (m.find()) {
      m.appendReplacement(sb, snakeCase2CamelCase(m.group(1)));
    }
    m.appendTail(sb);
    return sb.toString();
  }
}

