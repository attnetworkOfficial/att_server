package org.attnetwork.crypto;

import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;

import java.security.KeyPair;

public class ECKeyPair {
  private final BCECPrivateKey privateKey;
  private final BCECPublicKey publicKey;

  ECKeyPair(KeyPair keyPair) {
    this((BCECPrivateKey) keyPair.getPrivate(), (BCECPublicKey) keyPair.getPublic());
  }

  ECKeyPair(BCECPrivateKey privateKey, BCECPublicKey publicKey) {
    this.privateKey = privateKey;
    this.publicKey = publicKey;
  }

  public BCECPrivateKey getPrivateKey() {
    return privateKey;
  }

  public BCECPublicKey getPublicKey() {
    return publicKey;
  }
}
