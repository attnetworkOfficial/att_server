package org.attnetwork.proto.msg.wrapper;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class AtTnMsg extends AbstractSeqLanObject {
  public byte[] sessionId;
  public byte[] msgKey;
  public byte[] data;

//  public void pad(int blockSize) {
//    int d = raw.length % blockSize;
//    d += new Random().nextInt(100) * blockSize;
//  }

  public static final class Tail {
    public byte[] salt;
    public byte[] timestamp;
    // padding
  }
}
