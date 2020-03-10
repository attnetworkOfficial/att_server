package org.attnetwork.crypto.asymmetric;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface EncryptAsymmetric {
  AsmSignature sign(PrivateKey privateKey, byte[] data);

  boolean verify(byte[] publicKey, byte[] sign, byte[] data);

  boolean verify(PublicKey publicKey, byte[] sign, byte[] data);

  byte[] derivePublicKey(PrivateKey privateKey);
}
