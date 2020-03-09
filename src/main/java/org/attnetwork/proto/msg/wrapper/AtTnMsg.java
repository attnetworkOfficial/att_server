package org.attnetwork.proto.msg.wrapper;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public class AtTnMsg extends AbstractSeqLanObject implements WrappedMsg {
  public String sessionId;
  public WrapType wrap;
  public byte[] msg;

  @Override
  public WrapType getWrapType() {
    return wrap;
  }

  @Override
  public byte[] getMsg() {
    return msg;
  }
}
