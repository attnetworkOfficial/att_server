package org.attnetwork.server.component.l2.impl;

import org.attnetwork.crypto.ECCrypto;
import org.attnetwork.crypto.EncryptedData;
import org.attnetwork.crypto.asymmetric.AsmKeyPair;
import org.attnetwork.crypto.asymmetric.AsmPublicKey;
import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.crypto.asymmetric.AsmSignature;
import org.attnetwork.exception.AException;
import org.attnetwork.server.component.l2.EncryptServiceL2;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.spec.IEKeySpec;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EncryptServiceL2Impl implements EncryptServiceL2 {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final ECCrypto ecc = ECCrypto.instance();

  private AsmPublicKeyChain publicKeyChain;
  private BCECPrivateKey privateKey;
  private BCECPublicKey publicKey;

  private BCECPrivateKey getPrivateKey() {
    if (privateKey == null) {
      throw new AException("encrypt server has not initialized");
    }
    return privateKey;
  }

  @Override
  public void setup(AsmKeyPair keyPair) {
    if (!ecVerifyPublicKeyChain(keyPair.publicKeyChain, null).isValid) {
      throw new AException("not a valid key chain!");
    }
    if (keyPair.chainLength() == 0) {
      throw new AException("Using root-key is highly deprecated. For safety, generating and using its sub-keys!");
    }
    Long keyEndTimestamp = keyPair.publicKeyChain.key.endTimestamp;
    if (keyEndTimestamp == null) {
      throw new AException("Using a permanently valid sub-key is highly deprecated.");
    }
    if (keyEndTimestamp - System.currentTimeMillis() > 86400_000L * 30L) {
      throw new AException("Using a sub-key validation longer than 30 days is highly deprecated.");
    }
    this.publicKeyChain = keyPair.publicKeyChain;
    this.privateKey = ecc.restorePrivateKey(keyPair.privateKey);
    byte[] publicKey = keyPair.publicKeyChain.key.data;
    byte[] derivedPublicKey = ecc.derivePublicKey(this.privateKey);
    if (!ByteUtils.equals(publicKey, derivedPublicKey)) {
      throw new AException("not a valid private key!");
    }
    this.publicKey = ecc.restorePublicKey(keyPair.publicKeyChain.key.data);
  }

  @Override
  public byte[] ecDecrypt(byte[] data) {
    return ecc.decrypt(getPrivateKey(), data);
  }

  @Override
  public byte[] ecDecrypt(AsmPublicKey publicKey, byte[] data) {
    IEKeySpec ieKeySpec = new IEKeySpec(getPrivateKey(), ecc.restorePublicKey(publicKey.data));
    return ecc.decrypt(ieKeySpec, data);
  }

  @Override
  public EncryptedData ecEncrypt(AsmPublicKey publicKey, byte[] data) {
    IEKeySpec ieKeySpec = new IEKeySpec(getPrivateKey(), ecc.restorePublicKey(publicKey.data));
    return ecc.encrypt(ieKeySpec, data);
  }

  @Override
  public AsmSignature ecSign(byte[] data) {
    return ecc.sign(getPrivateKey(), data);
  }

  @Override
  public boolean ecVerify(AsmPublicKey publicKey, AsmSignature signature, byte[] data) {
    return ecc.verify(publicKey.data, signature.data, data);
  }

  @Override
  public AsmPublicKeyChain.Validation ecVerifyPublicKeyChain(AsmPublicKeyChain publicKeyChain, byte[] trusted) {
    return publicKeyChain.isValid(trusted, ecc);
  }

  @Override
  public AsmPublicKeyChain getPublicKeyChain() {
    if (publicKeyChain == null) {
      throw new AException("encrypt server has not initialized");
    }
    return publicKeyChain;
  }
}
