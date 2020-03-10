package org.attnetwork.crypto.asymmetric;

import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

public class AsmPublicKeyChain extends AbstractSeqLanObject {
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

  public Validation isValid(byte[] trusted, EncryptAsymmetric encrypt) {
    return isValid(this, trusted, encrypt);
  }

  private static Validation isValid(AsmPublicKeyChain publicKeyChain, byte[] trusted, EncryptAsymmetric encrypt) {
    long now = System.currentTimeMillis();
    if (!publicKeyChain.key.isValidTimestamp(now)) {
      return Validation.EXPIRED;
    }
    while (true) {
      if (ByteUtils.equals(trusted, publicKeyChain.key.raw)) {
        return Validation.VALID;
      }
      // if key is not trusted, using superKey to verify it
      byte[] checkData = publicKeyChain.key.getRaw();
      AsmSignature sign = publicKeyChain.sign;
      // load superKey
      publicKeyChain = publicKeyChain.superKey;
      if (publicKeyChain == null) {
        return Validation.NOT_TRUSTED;
      }
      if (!publicKeyChain.key.isValidTimestamp(now)) {
        return Validation.EXPIRED;
      }
      if (!encrypt.verify(publicKeyChain.key.raw, sign.raw, checkData)) {
        return Validation.INVALID_SIGN;
      }
    }
  }

  public enum Validation {
    VALID(true),
    EXPIRED(false),
    INVALID_SIGN(false),
    NOT_TRUSTED(false);

    public final boolean isValid;

    Validation(boolean isValid) {
      this.isValid = isValid;
    }
  }
}
