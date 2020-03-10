package org.attnetwork.proto.msg.wrapper;

import org.attnetwork.crypto.asymmetric.AsmPublicKey;

public class SignedMsg extends WrappedMsg {
  public byte[] sign;
  public AsmPublicKey publicKey;
}
