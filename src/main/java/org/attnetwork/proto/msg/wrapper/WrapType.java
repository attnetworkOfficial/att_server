package org.attnetwork.proto.msg.wrapper;

import java.math.BigInteger;
import java.util.HashMap;

public enum WrapType {
  SIGN(0x00),
  ENCRYPT(0x01);

  private static final HashMap<Integer, WrapType> codeMap;

  static {
    codeMap = new HashMap<>();
    for (WrapType op0 : values()) {
      WrapType op = codeMap.get(op0.opCode);
      if (op == null) {
        codeMap.put(op0.opCode, op0);
      } else {
        throw new RuntimeException(
            "duplicated opCode " + BigInteger.valueOf(op0.opCode).toString(16) +
            " for operation " + op + " & " + op0);
      }
    }
  }

  private final Integer opCode;

  WrapType(Integer opCode) {
    this.opCode = opCode;
  }

  public static WrapType getByCode(Integer code) {
    return codeMap.get(code);
  }
}