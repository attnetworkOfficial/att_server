package org.attnetwork.proto.sl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

class SeqLanObjWriter {

  static byte[] toByteArray(Object value) throws Exception {
    if (value == null) {
      return new byte[0];
    }
    switch (SeqLanDataType.getFieldType(value)) {
      case OBJECT:
      case ARRAY:
      case GENERIC_ARRAY:
      case LIST:
      case MAP:
        return multiToByteArray(value);
      case RAW:
        return (byte[]) value;
      case INTEGER:
        return BigInteger.valueOf((Integer) value).toByteArray();
      case LONG:
        return BigInteger.valueOf((Long) value).toByteArray();
      case BIG_INTEGER:
        return ((BigInteger) value).toByteArray();
      case BIG_DECIMAL:
        return bigDecimalToByteArray((BigDecimal) value);
      case STRING:
        return ((String) value).getBytes();
      default:
        throw new IllegalArgumentException("write unsupported class: " + value.getClass().getSimpleName());
    }
  }

  private static byte[] multiToByteArray(Object value) throws Exception {
    ByteArrayOutputStream cache = new ByteArrayOutputStream();
    switch (SeqLanDataType.getFieldType(value)) {
      case OBJECT:
        AbstractSeqLanObject msg = (AbstractSeqLanObject) value;
        if (msg.raw != null) {
          msg.writeWithoutLen(cache);
        } else {
          for (Field field : msg.getClass().getFields()) {
            SeqLan.writeLengthData(cache, toByteArray(field.get(msg)));
          }
        }
        break;
      case LIST:
        for (Object element : (List<?>) value) {
          SeqLan.writeLengthData(cache, toByteArray(element));
        }
        break;
      case ARRAY:
      case GENERIC_ARRAY:
        for (Object element : (Object[]) value) {
          SeqLan.writeLengthData(cache, toByteArray(element));
        }
        break;
      case MAP:
        for (Object o : ((Map) value).entrySet()) {
          SeqLan.writeLengthData(cache, toByteArray(((Map.Entry) o).getKey()));
          SeqLan.writeLengthData(cache, toByteArray(((Map.Entry) o).getValue()));
        }
        break;
      default:
        throw new IllegalAccessException("can't do final for: " + value);
    }
    return cache.toByteArray();
  }

  private static byte[] bigDecimalToByteArray(BigDecimal value) throws IOException {
    ByteArrayOutputStream cache = new ByteArrayOutputStream();
    int scale = value.stripTrailingZeros().scale();
    SeqLan.writeLengthData(cache, (scale == 0 ? value : value.movePointRight(scale)).toBigInteger().toByteArray());
    cache.write(BigInteger.valueOf(scale).toByteArray());
    return cache.toByteArray();
  }
}
