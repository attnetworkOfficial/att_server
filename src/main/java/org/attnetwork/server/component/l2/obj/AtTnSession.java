package org.attnetwork.server.component.l2.obj;

import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;

public class AtTnSession {
  public String algorithm;
  public Integer sessionId;
  public byte[] sharedSecret;
  public AsmPublicKeyChain userPublicKeyChain;
  public long createTimestamp;
  public long lastActiveTime;
}
