package org.attnetwork.proto.sl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import org.attnetwork.exception.AException;
import org.attnetwork.utils.ReflectUtil;

class Writer {
  private final ByteArrayOutputStream cache;
  private final Object value;

  private Writer(Object value) {
    this.cache = new ByteArrayOutputStream();
    this.value = value;
  }

  static void writeMessage(OutputStream os, AbstractMsg msg) {
    try {
      new Writer(msg).writeMessage(os);
    } catch (IOException | IllegalAccessException e) {
      throw new AException(e);
    }
  }

  private static void writeArray(OutputStream os, Object[] array) {
    try {
      new Writer(array).writeArray(os);
    } catch (IOException e) {
      throw new AException(e);
    }
  }

  //---------------------------

  private void write(Object value, Field field) throws IOException {
    if (value == null) {
      writeVarInt(cache, 0);
      return;
    }
    Class type = value.getClass();
    if (AbstractMsg.class.isAssignableFrom(type)) {
      writeMessage(cache, (AbstractMsg) value);
    } else if (List.class.isAssignableFrom(type)) {
      writeArray(cache, ReflectUtil.listToArray((List<?>) value, ReflectUtil.getFieldGenericType(field, 0)));
    } else if (type.isArray()) {
      if (type.getSimpleName().equals("byte[]")) {
        writeLengthData(cache, (byte[]) value);
      } else {
        writeArray(cache, (Object[]) value);
      }
    } else {
      writeLengthData(cache, toByteArray(value));
    }
  }

  private void writeMessage(OutputStream os) throws IOException, IllegalAccessException {
    if (value == null) {
      writeVarInt(os, 0);
      return;
    }
    Field[] fields = value.getClass().getFields();
    for (Field field : fields) {
      write(field.get(value), field);
    }
    writeLengthData(os, cache.toByteArray());
  }

  private void writeArray(OutputStream os) throws IOException {
    if (value == null) {
      writeVarInt(os, 0);
      return;
    }
    Object[] array = (Object[]) value;
    if (array.length == 0) {
      writeVarInt(os, 0);
      return;
    }
    for (Object o : array) {
      write(o, null);
    }
    writeLengthData(os, cache.toByteArray());
  }

  private static void writeLengthData(OutputStream os, byte[] data) throws IOException {
    writeVarInt(os, data.length);
    os.write(data);
  }

  private static byte[] toByteArray(Object value) throws IOException {
    switch (value.getClass().getSimpleName()) {
      case "Integer":
        return BigInteger.valueOf((Integer) value).toByteArray();
      case "Long":
        return BigInteger.valueOf((Long) value).toByteArray();
      case "BigInteger":
        return ((BigInteger) value).toByteArray();
      case "BigDecimal":
        return toByteArray((BigDecimal) value);
      case "String":
        return ((String) value).getBytes();
      default:
        throw new IllegalArgumentException("unsupported class: " + value.getClass().getSimpleName());
    }
  }

  private static byte[] toByteArray(BigDecimal value) throws IOException {
    ByteArrayOutputStream cache = new ByteArrayOutputStream();
    int scale = value.stripTrailingZeros().scale();
    writeLengthData(cache, (scale == 0 ? value : value.movePointRight(scale)).toBigInteger().toByteArray());
    cache.write(BigInteger.valueOf(scale).toByteArray());
    return cache.toByteArray();
  }


  private static void writeVarInt(OutputStream os, int varInt) throws IOException {
    if (varInt < 0) {
      throw new IllegalArgumentException();
    }
    if (varInt >= 0x80) {
      if (varInt >= 0x4000) {
        if (varInt >= 0x200000) {
          os.write((varInt >> 21) & 0x7F | 0x80);
        }
        os.write((varInt >> 14) & 0x7F | 0x80);
      }
      os.write((varInt >> 7) & 0x7F | 0x80);
    }
    os.write((varInt) & 0x7F);
  }
}
