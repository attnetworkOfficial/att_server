package org.attnetwork.proto.attn;

public abstract class AtTnKeyConverter {
  byte[] msgKeyPart1;
  byte[] msgKeyPart2;
  byte[] aesKeyPart1;
  byte[] aesKeyPart2;

  public abstract String aesMode();

  public abstract int aesBlockSize();

  public abstract byte[] msgKeyFromRaw(byte[] raw);

  public abstract AtTnKeys keysFromRaw(byte[] messageData);

  public abstract AtTnKeys keysFromMsgKey(byte[] msgKey);
}
