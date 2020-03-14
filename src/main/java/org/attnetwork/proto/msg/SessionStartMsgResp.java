package org.attnetwork.proto.msg;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class SessionStartMsgResp extends AbstractSeqLanObject {
  public String algorithm;
  public byte[] random;
  public Integer sessionId;
}
