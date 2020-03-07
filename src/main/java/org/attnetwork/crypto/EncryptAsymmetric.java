package org.attnetwork.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface EncryptAsymmetric {
  String getAsymmetricAlgorithm();

  byte[] sign(PrivateKey privateKey, byte[] data);

  boolean verify(PublicKey publicKey, byte[] sign, byte[] data);

  byte[] derivePublicKey(PrivateKey privateKey);
}
