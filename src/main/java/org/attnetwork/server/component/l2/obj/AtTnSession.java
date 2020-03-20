package org.attnetwork.server.component.l2.obj;

import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.proto.attn.AttnProto;

public class AtTnSession {
  public AttnProto proto;
  public Integer id;
  public byte[] sharedSecret;
  public byte[] salt;
  public byte[] oldSalt;
  public long saltTimestamp;
  public AsmPublicKeyChain userPublicKeyChain;
  public long createTimestamp;
  public long lastActiveTime;
}
