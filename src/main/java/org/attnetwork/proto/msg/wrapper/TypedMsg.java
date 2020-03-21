package org.attnetwork.proto.msg.wrapper;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class TypedMsg extends AbstractSeqLanObject {
  public String type;
  public byte[] data;

  public static TypedMsg wrap(String type, AbstractSeqLanObject msg) {
    TypedMsg typedMsg = new TypedMsg();
    typedMsg.type = type;
    typedMsg.data = msg == null ? null : msg.getRaw();
    return typedMsg;
  }
}
