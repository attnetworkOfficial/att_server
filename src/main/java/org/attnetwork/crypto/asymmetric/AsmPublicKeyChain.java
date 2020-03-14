package org.attnetwork.crypto.asymmetric;

import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

public final class AsmPublicKeyChain extends AbstractSeqLanObject {
  public AsmPublicKey key;
  public AsmSignature sign;
  public AsmPublicKeyChain superKey;

  @Override
  public String toString() {
    String s = "";
    if (superKey != null) {
      s += superKey.toString() + "\n\n";
    }
    s += key.toString();
    if (sign != null) {
      s += "\n" + sign.toString();
    }
    return s;
  }

  public AsmPublicKey rootKey() {
    AsmPublicKeyChain root = this;
    while (root.superKey != null) {
      root = root.superKey;
    }
    return root.key;
  }

  public Validation isValid(EncryptAsymmetric encrypt) {
    return isValid(this, null, encrypt);
  }

  public Validation isValid(byte[] trusted, EncryptAsymmetric encrypt) {
    return isValid(this, trusted, encrypt);
  }

  private static Validation isValid(AsmPublicKeyChain publicKeyChain, byte[] trusted, EncryptAsymmetric encrypt) {
    long now = System.currentTimeMillis();
    if (!publicKeyChain.key.isValidTimestamp(now)) {
      return Validation.EXPIRED;
    }
    while (true) {
      if (trusted != null && ByteUtils.equals(trusted, publicKeyChain.key.data)) {
        return Validation.VALID;
      }
      // if key is not trusted, using superKey to verify it
      byte[] checkData = publicKeyChain.key.getRaw();
      AsmSignature sign = publicKeyChain.sign;
      // load superKey
      publicKeyChain = publicKeyChain.superKey;
      if (publicKeyChain == null) {
        return trusted == null ? Validation.VALID : Validation.NOT_TRUSTED;
      }
      if (!publicKeyChain.key.isValidTimestamp(now)) {
        return Validation.EXPIRED;
      }
      if (!encrypt.verify(publicKeyChain.key.data, sign.data, checkData)) {
        return Validation.INVALID_KEY_SIGN;
      }
    }
  }

  public enum Validation {
    VALID(true),
    EXPIRED(false),
    INVALID_KEY_SIGN(false),
    NOT_TRUSTED(false);

    public final boolean isValid;

    Validation(boolean isValid) {
      this.isValid = isValid;
    }
  }
}
