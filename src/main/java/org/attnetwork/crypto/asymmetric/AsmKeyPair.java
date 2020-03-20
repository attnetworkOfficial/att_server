package org.attnetwork.crypto.asymmetric;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class AsmKeyPair extends AbstractSeqLanObject {
  public byte[] privateKey;
  public AsmPublicKeyChain publicKeyChain;

  public int chainLength() {
    return publicKeyChain.chainLength();
  }
}
