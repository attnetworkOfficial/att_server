package org.attnetwork.crypto.asymmetric;

import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

public final class AsmSignature extends AbstractSeqLanObject {
  public String algorithm;
  public byte[] data;

  @Override
  public String toString() {
    return "├ signature ────────────────────" +
        (algorithm /**/ == null ? "" : "\n│ algorithm: " + algorithm) +
        (data      /**/ == null ? "" : "\n└ hex:       " + ByteUtils.toHexString(data));
  }

  public static AsmSignature build(String algorithm, byte[] raw) {
    AsmSignature s = new AsmSignature();
    s.algorithm = algorithm;
    s.data = raw;
    return s;
  }
}
