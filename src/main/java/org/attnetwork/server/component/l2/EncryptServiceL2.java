package org.attnetwork.server.component.l2;

import org.attnetwork.crypto.EncryptedData;
import org.attnetwork.crypto.asymmetric.AsmKeyPair;
import org.attnetwork.crypto.asymmetric.AsmPublicKey;
import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.crypto.asymmetric.AsmSignature;

public interface EncryptServiceL2 {
  void setup(AsmKeyPair keyPair);

  byte[] ecDecrypt(byte[] encryptedData);

  byte[] ecDecrypt(AsmPublicKey publicKey, byte[] data);

  EncryptedData ecEncrypt(AsmPublicKey publicKey, byte[] data);

  AsmSignature ecSign(byte[] data);

  boolean ecVerify(AsmPublicKey publicKey, AsmSignature signature, byte[] data);

  AsmPublicKeyChain.Validation ecVerifyPublicKeyChain(AsmPublicKeyChain publicKeyChain, byte[] trusted);

  AsmPublicKeyChain getPublicKeyChain();
}
