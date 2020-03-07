package org.attnetwork.proto.sl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import org.attnetwork.exception.AException;
import org.attnetwork.utils.BitmapFlags;
import org.attnetwork.utils.ReflectUtil;

class SeqLanObjReader {

  static <T extends AbstractSeqLanObject> T read(byte[] raw, Class<T> msgType, int readFieldLimits) {
    try {
      return new SeqLanObjReader(raw, readFieldLimits).read(msgType, null);
    } catch (IllegalAccessException | InstantiationException e) {
      throw new AException(e);
    }
  }

  private int index;
  private int length;
  private int fields;

  private final byte[] raw;
  private final int readFieldLimits;

  private SeqLanObjReader(byte[] raw, int readFieldLimits) {
    this.raw = raw;
    this.readFieldLimits = readFieldLimits;
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
    if (fields++ > readFieldLimits) {
      return null;
    }
    switch (SeqLanDataType.getClassType(type)) {
      case OBJECT:
        return readMessage(type);
      case ARRAY: // unknown array length, read as list first
        return (T) ReflectUtil.listToArray(readList(type.getComponentType()), type.getComponentType());
      case LIST:
        if (field == null) {
          throw new IllegalArgumentException("Type List<List<?>> is not supported now");
        }
        return (T) readList(ReflectUtil.getFieldGenericType(field, 0));
      // simple data
      case RAW:
        return (T) readRaw();
      case BITMAP_FLAGS:
        return (T) BitmapFlags.load((Class<Enum>) ReflectUtil.getFieldGenericType(field, 0), readRaw());
      case INTEGER:
        return (T) (Integer) readBigInteger().intValueExact();
      case LONG:
        return (T) (Long) readBigInteger().longValueExact();
      case BIG_INTEGER:
        return (T) readBigInteger();
      case BIG_DECIMAL:
        return (T) readBigDecimal();
      case STRING:
        return (T) new String(raw, index, length);
      default:
        throw new IllegalArgumentException("unsupported class: " + type.getSimpleName());
    }
  }

  // object and list
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

  // simple data
  private byte[] readRaw() {
    byte[] data = new byte[length];
    System.arraycopy(raw, index, data, 0, length);
    return data;
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

  // length
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
