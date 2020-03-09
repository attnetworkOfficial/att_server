package org.attnetwork.proto.msg.wrapper;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public class SignedMsg extends AbstractSeqLanObject implements WrappedMsg {
  public WrapType wrap;
  public byte[] msg;
  public byte[] sign;
  public byte[] publicKey;

  @Override
  public WrapType getWrapType() {
    return null;
  }

  @Override
  public byte[] getMsg() {
    return msg;
  }
}
