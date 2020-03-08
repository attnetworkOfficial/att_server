package org.attnetwork.proto.msg.comp;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public class Signature extends AbstractSeqLanObject {
  public byte[] sign;
  public byte[] publicKey;
}
