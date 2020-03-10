package org.attnetwork.crypto.asymmetric;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public class AsmKeyPair extends AbstractSeqLanObject {
  public byte[] privateKey;
  public AsmPublicKeyChain publicKeyChain;
}
