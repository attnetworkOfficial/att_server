package org.attnetwork.proto.sl;

import java.util.List;

public enum SeqLanDataType {
  UNKNOWN(""),
  // raw
  RAW(SeqLan.RAW),
  BITMAP_FLAGS(SeqLan.RAW),

  // number
  INTEGER(SeqLan.NUMBER),
  LONG(SeqLan.NUMBER),
  BIG_INTEGER(SeqLan.NUMBER),

  // decimal
  BIG_DECIMAL(SeqLan.DECIMAL),

  // string
  STRING(SeqLan.STRING),

  // array
  LIST(SeqLan.ARRAY),
  ARRAY(SeqLan.ARRAY),

  // object
  OBJECT(SeqLan.OBJECT),
  //
  ;

  private final String slType;

  SeqLanDataType(String slType) {
    this.slType = slType;
  }

  public String getSeqLanType() {
    return slType;
  }


  public static SeqLanDataType getFieldType(Object value) {
    return getClassType(value.getClass());
  }

  public static SeqLanDataType getClassType(Class<?> type) {
    if (AbstractSeqLanObject.class.isAssignableFrom(type)) {
      return OBJECT;
    } else if (List.class.isAssignableFrom(type)) {
      return LIST;
    } else if (type.isArray()) {
      if (byte[].class.isAssignableFrom(type)) {
        return RAW;
      } else {
        return ARRAY;
      }
    } else {
      switch (type.getSimpleName()) {
        case "Integer":
          return INTEGER;
        case "Long":
          return LONG;
        case "BigInteger":
          return BIG_INTEGER;
        case "BigDecimal":
          return BIG_DECIMAL;
        case "String":
          return STRING;
        case "BitmapFlags":
          return BITMAP_FLAGS;
        default:
          return UNKNOWN;
      }
    }
  }
}
