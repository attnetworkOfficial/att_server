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
    s += key.toString();
    if (sign != null) {
      s += "\n" + sign.toString();
    } else {
      s += "\n└───────────────────────────────";
    }
    int d = 0;
    if (superKey != null) {
      ++d;
      StringBuilder rep = new StringBuilder();
      for (int i = 0; i < d; i++) {
        rep.append("  ");
      }
      rep.append("$1");
      s += ("\n└ \uD83D\uDD17 " + (superKey.superKey == null ? "ROOT KEY \uD83D\uDD0F" : "SUPER KEY \uD83D\uDD12") + "\n" + superKey.toString()).replaceAll("(.+)", rep.toString());
    }
    return s;
  }

  public int chainLength() {
    int l = 0;
    AsmPublicKeyChain superKey = this.superKey;
    while (superKey != null) {
      l++;
      superKey = superKey.superKey;
    }
    return l;
  }

  public AsmPublicKey rootKey() {
    AsmPublicKeyChain root = this;
    while (root.superKey != null) {
      root = root.superKey;
    }
    return root.key;
  }

  public void makePublicKeyTimeStampReasonable() {
    key.startTimestamp = maxStartTimestamp();
    key.endTimestamp = minEndTimestamp();
  }

  public Long maxStartTimestamp() {
    Long s = key.startTimestamp;
    AsmPublicKeyChain superKey = this.superKey;
    while (superKey != null) {
      Long ss = superKey.key.startTimestamp;
      if (ss != null) {
        s = s == null ? ss : Math.max(ss, s);
      }
      superKey = superKey.superKey;
    }
    return s;
  }

  public Long minEndTimestamp() {
    Long e = key.endTimestamp;
    AsmPublicKeyChain superKey = this.superKey;
    while (superKey != null) {
      Long se = superKey.key.endTimestamp;
      if (se != null) {
        e = e == null ? se : Math.min(se, e);
      }
      superKey = superKey.superKey;
    }
    return e;
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
