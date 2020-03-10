package org.attnetwork.crypto.asymmetric;

import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

public class AsmSignature {
  public String algorithm;
  public byte[] raw;

  @Override
  public String toString() {
    return "---- signature info ----" +
           "\n algorithm: " + algorithm +
           "\n hex:       " + ByteUtils.toHexString(raw);
  }

  public static AsmSignature build(String algorithm, byte[] raw) {
    AsmSignature s = new AsmSignature();
    s.algorithm = algorithm;
    s.raw = raw;
    return s;
  }
}
