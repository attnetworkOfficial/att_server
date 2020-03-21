package org.attnetwork.proto.msg.wrapper;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class AtTnOriginMsg extends AbstractSeqLanObject {
  public byte[] data; // typed message
  public byte[] salt;
  public Long timestamp;
  public byte[] padding;
}
