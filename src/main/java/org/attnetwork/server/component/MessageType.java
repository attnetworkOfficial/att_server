package org.attnetwork.server.component;

import org.attnetwork.utils.HashUtil;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import java.util.HashSet;

public class MessageType {
  public static final String __DEBUG__ = "0";

  public static final String START_SESSION = "010d92";
  public static final String PING = "a73aab";
  public static final String QUERY_CHATS = "06a652";
  public static final String QUERY_CONTACTS = "aa2cfd";


  public static void main(String[] args) {
    HashSet<String> checker = new HashSet<>();
    int len = 6;
    for (String name : new String[]{
        "start_session",
        "ping",
        // chat server
        "query_chats",
        "query_contacts",
    }) {
      String code = convert(name, checker, len);
      System.out.println("public static final String " + name.toUpperCase() + " = \"" + code + "\";");
    }
  }

  private static String convert(String name, HashSet<String> checker, int len) {
    String code = ByteUtils.toHexString(HashUtil.SHA3_256.hash(name.getBytes())).substring(0, len);
    boolean add = checker.add(code);
    if (!add) {
      throw new RuntimeException("duplicated method: " + name + "|" + code);
    }
    return code;
  }
}
