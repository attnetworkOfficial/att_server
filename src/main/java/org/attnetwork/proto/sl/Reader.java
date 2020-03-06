package org.attnetwork.proto.sl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.attnetwork.exception.AException;
import org.attnetwork.utils.ReflectUtil;

class Reader {
  private int index;
  private int length;

  private final byte[] raw;

  static <T extends AbstractMsg> T read(byte[] raw, Class<T> msgType) {
    try {
      return new Reader(raw).read(msgType, null);
    } catch (IllegalAccessException | InstantiationException e) {
      throw new AException(e);
    }
  }

  private Reader(byte[] raw) {
    this.raw = raw;
  }

  private <T> T read(Class<T> type, Field field) throws IllegalAccessException, InstantiationException {
    readLength();
    if (length == 0) {
      index++;
      return null;
    } else {
      int next = length + index;
      T obj = readCurrentObject(type, field);
      index = next;
      return obj;
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T readCurrentObject(Class<T> type, Field field) throws InstantiationException, IllegalAccessException {
    if (AbstractMsg.class.isAssignableFrom(type)) {
      return readMessage(type);
    } else if (List.class.equals(type)) {
      if (field == null) {
        throw new IllegalArgumentException("Type List<List<?>> is not supported now");
      }
      return (T) readList(ReflectUtil.getFieldGenericType(field, 0));
    } else if (type.isArray()) {
      return readArray(type);
    } else {
      return (T) readSimpleObject(type);
    }
  }

  private <T> T readMessage(Class<T> type) throws IllegalAccessException, InstantiationException {
    Field[] fields = type.getFields();
    T msg = type.newInstance();
    for (Field field : fields) {
      Object value = read(field.getType(), field);
      if (value != null) {
        field.set(msg, value);
      }
    }
    return msg;
  }

  private <T> ArrayList<T> readList(Class<T> elementType) throws InstantiationException, IllegalAccessException {
    ArrayList<T> list = new ArrayList<>();
    int start = index;
    int end = index + length;
    while (end > index) {
      list.add(read(elementType, null));
    }
    index = start;
    return list;
  }

  @SuppressWarnings("unchecked")
  private <T> T readArray(Class<T> type) throws InstantiationException, IllegalAccessException {
    if ("byte[]".equals(type.getSimpleName())) {
      byte[] data = new byte[length];
      System.arraycopy(raw, index, data, 0, length);
      return (T) data;
    } else {
      return (T) ReflectUtil.listToArray(readList(type.getComponentType()), type.getComponentType());
    }
  }

  private Object readSimpleObject(Class<?> type) {
    switch (type.getSimpleName()) {
      case "Integer":
        return readBigInteger().intValueExact();
      case "Long":
        return readBigInteger().longValueExact();
      case "BigInteger":
        return readBigInteger();
      case "BigDecimal":
        return readBigDecimal();
      case "String":
        return new String(raw, index, length);
      default:
        throw new IllegalArgumentException("unsupported class: " + type.getSimpleName());
    }
  }

  private BigInteger readBigInteger() {
    byte[] bytes = new byte[length];
    System.arraycopy(raw, index, bytes, 0, length);
    return new BigInteger(bytes);
  }

  private BigDecimal readBigDecimal() {
    int len = length;
    readLength();
    BigInteger value = readBigInteger();
    index += length;
    length = len - varIntLength(length) - length;
    return new BigDecimal(value, readBigInteger().intValueExact());
  }

  private void readLength() {
    length = raw[index] & 0x7F;
    while ((raw[index++] >>> 7) > 0) {
      length = length << 7;
      length += raw[index] & 0x7F;
    }
  }

  private static int varIntLength(int varInt) {
    if (varInt >= 0x80) {
      if (varInt >= 0x4000) {
        if (varInt >= 0x200000) {
          return 4;
        }
        return 3;
      }
      return 2;
    }
    return 1;
  }
}
