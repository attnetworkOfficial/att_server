package org.attnetwork.proto.msg.wrapper;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class AtTnEncryptedMsg extends AbstractSeqLanObject {
  public byte[] sessionId;
  public byte[] msgKey;
  public byte[] data;
}
