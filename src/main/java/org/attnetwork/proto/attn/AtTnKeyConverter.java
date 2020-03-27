package org.attnetwork.proto.attn;

public interface AtTnKeyConverter {
  String aesMode();

  int aesBlockSize();

  byte[] msgKeyFromRaw(byte[] raw);

  AtTnKeys keysFromRaw(byte[] messageData);

  AtTnKeys keysFromMsgKey(byte[] msgKey);
}
