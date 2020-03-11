package org.attnetwork.crypto;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class EncryptedData extends AbstractSeqLanObject {
  public String algorithm;
  public byte[] data;

  public static EncryptedData build(String algorithm, byte[] data) {
    EncryptedData d = new EncryptedData();
    d.algorithm = algorithm;
    d.data = data;
    return d;
  }
}
