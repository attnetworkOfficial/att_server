package org.attnetwork.proto.msg.wrapper;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public abstract class WrappedMsg extends AbstractSeqLanObject {
  public Integer wrapType;
  public byte[] msg;

  public void wrap(WrapType wrapType, byte[] msg) {
    this.wrapType = wrapType.getCode();
    this.msg = msg;
  }
}
