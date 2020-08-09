package org.attnetwork.proto.msg;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class ChatMsg extends AbstractSeqLanObject {
  public Integer id;
  public Type type;
  public String title;
  public String username;
  public String first_name;
  public String last_name;

  public enum Type {
    PRIVATE,
    GROUP,
    SUPER_GROUP,
    CHANNEL,
  }
}
