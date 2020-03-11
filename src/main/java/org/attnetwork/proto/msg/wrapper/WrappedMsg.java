package org.attnetwork.proto.msg.wrapper;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class WrappedMsg extends AbstractSeqLanObject {
  public Integer wrapType;
  public byte[] data;

  public static WrappedMsg wrap(WrapType wrapType, byte[] data) {
    WrappedMsg w = new WrappedMsg();
    w.wrapType = wrapType.getCode();
    w.data = data;
    return w;
  }
}
