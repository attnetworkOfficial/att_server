package org.attnetwork.proto.msg.wrapper;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class TypedMsg extends AbstractSeqLanObject {
  public String type;
  public byte[] data;

  public static final String START_SESSION = "start_session";
}
