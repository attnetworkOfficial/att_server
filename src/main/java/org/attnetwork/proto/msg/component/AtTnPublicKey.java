package org.attnetwork.proto.msg.component;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public class AtTnPublicKey extends AbstractSeqLanObject {
  public String algorithm;
  public Long startTimestamp;
  public Long endTimestamp;
  public String desc;
  public byte[] encoded;
}
