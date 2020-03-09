package org.attnetwork.proto.sl;

import java.math.BigInteger;
import java.util.HashMap;

public enum SeqLanOperation {
  TEST(0x00),
  TEST1(0x01);

  private static final HashMap<Integer, SeqLanOperation> codeMap;

  static {
    codeMap = new HashMap<>();
    for (SeqLanOperation op0 : values()) {
      SeqLanOperation op = codeMap.get(op0.opCode);
      if (op == null) {
        codeMap.put(op0.opCode, op0);
      } else {
        throw new RuntimeException(
            "duplicated opCode " + BigInteger.valueOf(op0.opCode).toString(16) +
            " for operation " + op + " & " + op0);
      }
    }
  }

  private final int opCode;

  SeqLanOperation(int opCode) {
    this.opCode = opCode;
  }

  public static SeqLanOperation getByCode(Integer code) {
    return codeMap.get(code);
  }
}
