package org.attnetwork.proto.attn;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AtTnKeys {
  public byte[] msgKey;
  public SecretKeySpec aesKey;
  public GCMParameterSpec aesIv;

  @Override
  public String toString() {
    return "\nmsgKey: " + ByteUtils.toHexString(msgKey) +
        "\naesKey: " + ByteUtils.toHexString(aesKey.getEncoded()) +
        "\naesIv:  " + ByteUtils.toHexString(aesIv.getIV());
  }
}
