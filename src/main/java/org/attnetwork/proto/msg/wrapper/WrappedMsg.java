package org.attnetwork.proto.msg.wrapper;

public interface WrappedMsg {
  WrapType getWrapType();

  byte[] getMsg();
}
