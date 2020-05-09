package org.attnetwork.crypto.asymmetric;

import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.attnetwork.utils.DateUtil;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

public final class AsmPublicKey extends AbstractSeqLanObject {
  public String algorithm;
  public Long startTimestamp;
  public Long endTimestamp;
  public Long createTimestamp;
  /**
   * The {@code proof} is something that can prove the {@code createTimestamp} of this key.
   * For example, the newest Bitcoin block hash when creating the key.
   */
  public String proof;
  public String desc;
  public byte[] data;

  public static AsmPublicKey preGen() {
    AsmPublicKey s = new AsmPublicKey();
    s.createTimestamp = System.currentTimeMillis();
    return s;
  }

  public AsmPublicKey algorithm(String algorithm) {
    this.algorithm = algorithm;
    return this;
  }

  public AsmPublicKey start(Long start) {
    this.startTimestamp = start;
    return this;
  }

  public AsmPublicKey end(Long end) {
    this.endTimestamp = end;
    return this;
  }

  public AsmPublicKey proof(String proof) {
    this.proof = proof;
    return this;
  }

  public AsmPublicKey desc(String desc) {
    this.desc = desc;
    return this;
  }

  public AsmPublicKey data(byte[] data) {
    this.data = data;
    return this;
  }


  @Override
  public String toString() {
    return "┌ public key info ──────────────" +
        (algorithm      /**/ == null ? "" : "\n│ algorithm:      " + algorithm) +
        (startTimestamp /**/ == null ? "" : "\n│ invalid before: " + DateUtil.toHumanString(startTimestamp)) +
        (endTimestamp   /**/ == null ? "" : "\n│ invalid after:  " + DateUtil.toHumanString(endTimestamp)) +
        (createTimestamp/**/ == null ? "" : "\n│ create time:    " + DateUtil.toHumanString(createTimestamp)) +
        (proof          /**/ == null ? "" : "\n│ proof:          " + proof) +
        (desc           /**/ == null ? "" : "\n│ description:    " + desc) +
        (data           /**/ == null ? "" : "\n│ key:            " + ByteUtils.toHexString(data));
  }


  @Override
  public int hashCode() {
    return ByteUtils.deepHashCode(data);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof AsmPublicKey) {
      return ByteUtils.equals(data, ((AsmPublicKey) o).data);
    }
    return false;
  }

  public boolean isValidTimestamp() {
    return isValidTimestamp(System.currentTimeMillis());
  }

  public boolean isValidTimestamp(long now) {
    return (startTimestamp == null || startTimestamp <= now) &&
        (endTimestamp == null || now <= endTimestamp);
  }
}
