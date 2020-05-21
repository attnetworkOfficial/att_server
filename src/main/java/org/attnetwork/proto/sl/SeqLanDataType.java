package org.attnetwork.proto.sl;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

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
  GENERIC_ARRAY(SeqLan.ARRAY),

  // dict
  MAP(SeqLan.DICT),

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

  public static SeqLanDataType getTypeType(Type type) {
    if (type instanceof Class) {
      return getClassType((Class<?>) type);
    } else {
      if (type instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) type;
        return getTypeType(parameterizedType.getRawType());
      } else if (type instanceof GenericArrayType) {
        return GENERIC_ARRAY;
      }
    }
    return UNKNOWN;
  }

  public static SeqLanDataType getClassType(Class<?> clazz) {
    if (AbstractSeqLanObject.class.isAssignableFrom(clazz)) {
      return OBJECT;
    } else if (List.class.isAssignableFrom(clazz)) {
      return LIST;
    } else if (Map.class.isAssignableFrom(clazz)) {
      return MAP;
    } else if (clazz.isArray()) {
      if (byte[].class.isAssignableFrom(clazz)) {
        return RAW;
      } else {
        return ARRAY;
      }
    } else {
      switch (clazz.getSimpleName()) {
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
