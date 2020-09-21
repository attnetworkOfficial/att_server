package org.attnetwork.proto.msg;

import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.attnetwork.utils.BitmapFlags;

public final class Chat extends AbstractSeqLanObject {
  public Integer id;
  public BitmapFlags<Flags> flags;
  public String title;
  public ChatPhoto photo;
  public Integer membersCount;

  public enum Flags {
    // user flags
    IS_CREATOR,
    IS_KICKED,
    IS_LEFT,
    IS_DEACTIVATED,
  }
}
