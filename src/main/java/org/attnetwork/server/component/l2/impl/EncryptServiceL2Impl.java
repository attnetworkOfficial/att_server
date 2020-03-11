package org.attnetwork.server.component.l2.impl;

import org.attnetwork.crypto.ECCrypto;
import org.attnetwork.crypto.EncryptedData;
import org.attnetwork.crypto.asymmetric.AsmKeyPair;
import org.attnetwork.crypto.asymmetric.AsmPublicKey;
import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.crypto.asymmetric.AsmSignature;
import org.attnetwork.server.component.l2.EncryptServiceL2;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.springframework.stereotype.Service;

@Service
public class EncryptServiceL2Impl implements EncryptServiceL2 {

  private final ECCrypto ecc = ECCrypto.instance();

  private AsmPublicKeyChain publicKeyChain;
  private BCECPrivateKey privateKey;

  @Override
  public void setup(AsmKeyPair keyPair) {
    this.publicKeyChain = keyPair.publicKeyChain;
    this.privateKey = ecc.restorePrivateKey(keyPair.privateKey);
  }

  @Override
  public byte[] ecDecrypt(byte[] data) {
    return ecc.decrypt(privateKey, data);
  }

  @Override
  public AsmSignature ecSign(byte[] data) {
    return ecc.sign(privateKey, data);
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
    return publicKeyChain;
  }
}
