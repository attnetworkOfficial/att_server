package org.attnetwork.proto.sl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import org.attnetwork.exception.AException;

class SeqLanObjWriter {

  static byte[] toByteArray(AbstractSeqLanObject msg) {
    try {
      return wrap(msg).doFinal();
    } catch (IOException | IllegalAccessException e) {
      throw new AException(e);
    }
  }

  private final ByteArrayOutputStream cache;
  private final Object value;

  private SeqLanObjWriter(Object value) {
    this.cache = new ByteArrayOutputStream();
    this.value = value;
  }

  private static SeqLanObjWriter wrap(Object object) {
    return new SeqLanObjWriter(object);
  }

  private byte[] doFinal() throws IllegalAccessException, IOException {
    if (value == null) {
      return null;
    }
    switch (SeqLanDataType.getFieldType(value)) {
      case OBJECT:
        AbstractSeqLanObject msg = (AbstractSeqLanObject) value;
        if (msg.raw != null) {
          // already done
          return msg.raw;
        }
        for (Field field : value.getClass().getFields()) {
          writeCache(field.get(value));
        }
        break;
      case LIST:
        for (Object element : (List<?>) value) {
          writeCache(element);
        }
        break;
      case ARRAY:
        for (Object element : (Object[]) value) {
          writeCache(element);
        }
        break;
      default:
        throw new IllegalAccessException("can't do final for: " + value);
    }
    return cache.toByteArray();
  }

  private void writeCache(Object value) throws IOException, IllegalAccessException {
    if (value == null) {
      writeVarInt(cache, 0);
    } else {
      writeLengthData(cache, toByteArray(value));
    }
  }

  private static byte[] toByteArray(Object value) throws IOException, IllegalAccessException {
    switch (SeqLanDataType.getFieldType(value)) {
      case OBJECT:
      case ARRAY:
      case LIST:
        return wrap(value).doFinal();
      case RAW:
        return (byte[]) value;
      case INTEGER:
        return BigInteger.valueOf((Integer) value).toByteArray();
      case LONG:
        return BigInteger.valueOf((Long) value).toByteArray();
      case BIG_INTEGER:
        return ((BigInteger) value).toByteArray();
      case BIG_DECIMAL:
        return toByteArray((BigDecimal) value);
      case STRING:
        return ((String) value).getBytes();
      default:
        throw new IllegalArgumentException("unsupported class: " + value.getClass().getSimpleName());
    }
  }

  static void writeLengthData(OutputStream os, byte[] data) throws IOException {
    if (data == null || data.length == 0) {
      os.write(0);
    } else {
      writeVarInt(os, data.length);
      os.write(data);
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
