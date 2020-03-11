package org.attnetwork.crypto.asymmetric;

import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.attnetwork.utils.DateUtil;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

public final class AsmPublicKey extends AbstractSeqLanObject {
  public String algorithm;
  public Long startTimestamp;
  public Long endTimestamp;
  public String desc;
  public byte[] data;

  @Override
  public String toString() {
    return "---- public key info ----" +
           "\nalgorithm:      " + algorithm +
           "\ninvalid before: " + DateUtil.toHumanString(startTimestamp) +
           "\ninvalid after:  " + DateUtil.toHumanString(endTimestamp) +
           "\ndescription:    " + desc +
           "\nkey:            " + ByteUtils.toHexString(data);
  }

  public boolean isValidTimestamp() {
    return isValidTimestamp(System.currentTimeMillis());
  }

  public boolean isValidTimestamp(long now) {
    return (startTimestamp == null || startTimestamp <= now) &&
           (endTimestamp == null || now <= endTimestamp);
  }
}
