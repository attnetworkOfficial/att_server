package org.attnetwork.proto.msg.component;

import org.attnetwork.crypto.EncryptAsymmetric;
import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

public class AsymmetricPublicKeyChain extends AbstractSeqLanObject {
  public AtTnPublicKey key;
  public byte[] sign;
  public AsymmetricPublicKeyChain superKey;

  // check if this key.encoded key is valid
  public boolean isValid(byte[] rootKey, EncryptAsymmetric encrypt) {
    AtTnPublicKey publicKey = this.key;
    while (true) {
      if (ByteUtils.equals(publicKey.encoded, rootKey)) {
        return true;
      }
      publicKey = superKey.key;
      if (publicKey == null || !encrypt.verify(publicKey.encoded, sign, key.getRaw())) {
        return false;
      }
    }
  }
}
