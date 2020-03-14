package org.attnetwork.proto.msg.wrapper;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public enum WrapType {
  ATTN_PROTO(0x00),
  SIGN(0x04),
  ENCRYPT(0x08);

  public static final List<WrapType> L_ENCRYPT_SIGN = Arrays.asList(WrapType.ENCRYPT, WrapType.SIGN);
  public static final List<WrapType> L_SIGN_ENCRYPT = Arrays.asList(WrapType.SIGN, WrapType.ENCRYPT);

  private static final HashMap<Integer, WrapType> codeMap;

  static {
    codeMap = new HashMap<>();
    for (WrapType op0 : values()) {
      WrapType op = codeMap.get(op0.code);
      if (op == null) {
        codeMap.put(op0.code, op0);
      } else {
        throw new RuntimeException(
            "duplicated code " + BigInteger.valueOf(op0.code).toString(16) +
            " for operation " + op + " & " + op0);
      }
    }
  }

  private final Integer code;

  WrapType(Integer code) {
    this.code = code;
  }

  public Integer getCode() {
    return code;
  }

  public static WrapType getByCode(Integer code) {
    return codeMap.get(code);
  }
}