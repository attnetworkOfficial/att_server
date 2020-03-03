package org.attnetwork.proto.sl;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.attnetwork.exception.AException;

class Reader {
  private int index;
  private int length;

  private final byte[] raw;

  static <T extends AbstractMsg> T read(byte[] raw, Class<T> msgType) {
    try {
      return new Reader(raw).read(msgType);
    } catch (IllegalAccessException | InstantiationException e) {
      throw new AException(e);
    }
  }

  private Reader(byte[] raw) {
    this.raw = raw;
  }

  @SuppressWarnings("unchecked")
  private <T> T read(Class<T> type) throws IllegalAccessException, InstantiationException {
    readLength();
    if (length == 0) {
      return null;
    }
    T obj = AbstractMsg.class.isAssignableFrom(type) ? readMessage(type) : readNotMessage(type);
    moveIndex();
    return obj;
  }

  private <T> T readMessage(Class<T> type) throws IllegalAccessException, InstantiationException {
    Field[] fields = type.getFields();
    T msg = type.newInstance();
    for (Field field : fields) {
      Object value = read(field.getType());
      if (value != null) {
        field.set(msg, value);
      }
    }
    return msg;
  }

  @SuppressWarnings("unchecked")
  private <T> T readNotMessage(Class<T> type) throws IllegalAccessException, InstantiationException {
    return type.isArray() ? readArray(type) : (T) readSimpleObject(type);
  }

  @SuppressWarnings("unchecked")
  private <T> T readArray(Class<T> type) throws InstantiationException, IllegalAccessException {
    if ("byte[]".equals(type.getSimpleName())) {
      byte[] data = new byte[length];
      System.arraycopy(raw, index, data, 0, length);
      return (T) data;
    } else {
      int count = readVarInt();
      T array = (T) Array.newInstance(type.getComponentType(), count);
      Class<?> componentType = array.getClass().getComponentType();
      for (int i = 0; i < count; i++) {
        Array.set(array, i, read(componentType));
      }
      return array;
    }
  }

  private void readLength() {
    length = readVarInt();
  }


  private int readVarInt() {
    int varInt = raw[index] & 0x7F;
    while ((raw[index++] >>> 7) > 0) {
      varInt = varInt << 7;
      varInt += raw[index] & 0x7F;
    }
    return varInt;
  }

  private void moveIndex() {
    index += length;
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
        BigInteger scale = readBigInteger();
        moveIndex();
        readLength();
        BigInteger value = readBigInteger();
        return new BigDecimal(value, scale.intValueExact());
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
}
