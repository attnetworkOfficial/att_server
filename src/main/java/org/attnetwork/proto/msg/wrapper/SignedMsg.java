package org.attnetwork.proto.msg.wrapper;

import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.crypto.asymmetric.AsmSignature;
import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class SignedMsg extends AbstractSeqLanObject {
  public AsmSignature sign;
  public AsmPublicKeyChain publicKeyChain;
  public byte[] data;
}
