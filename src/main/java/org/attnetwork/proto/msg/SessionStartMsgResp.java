package org.attnetwork.proto.msg;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class SessionStartMsgResp extends AbstractSeqLanObject {
  public String version;
  public Integer sessionId;
  public byte[] random;
  public byte[] salt;
}
