package org.attnetwork.proto.attn;

import java.util.HashMap;

public class AttnProto {
  public static final AttnProto DEBUG = new AttnProto("debug", 8, 1000, 64);
  public static final AttnProto V1_0 = new AttnProto("1.0", 8, 30 * (60 * 1000), 64);

  public static AttnProto getByVersion(String version) {
    return MAP.get(version);
  }

  private static final HashMap<String, AttnProto> MAP = new HashMap<>();

  static {
    MAP.put("1.0", V1_0);
    MAP.put("debug", DEBUG);
  }


  private AttnProto(
      String VERSION,
      int SERVER_SALT_SIZE,
      int SERVER_SALT_UPDATE_TIME,
      int SESSION_START_RANDOM_SIZE) {

    this.VERSION = VERSION;
    this.SERVER_SALT_SIZE = SERVER_SALT_SIZE;
    this.SESSION_START_RANDOM_SIZE = SESSION_START_RANDOM_SIZE;
    this.SERVER_SALT_UPDATE_TIME = SERVER_SALT_UPDATE_TIME;
  }

  public final String VERSION;
  public final int SERVER_SALT_SIZE; //bytes
  public final int SESSION_START_RANDOM_SIZE; // bytes;
  public final long SERVER_SALT_UPDATE_TIME; // ms
}
