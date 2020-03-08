package org.attnetwork.proto.sl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import org.attnetwork.exception.AException;
import org.attnetwork.proto.sl.AbstractSeqLanObject.ProcessFieldData;
import org.attnetwork.utils.BitmapFlags;
import org.attnetwork.utils.ReflectUtil;

class SeqLanObjReader {

  static <T extends AbstractSeqLanObject> T read(InputStream source, Class<T> msgType) {
    try {
      return wrap(source).read(msgType, null);
    } catch (Exception e) {
      throw new AException(e);
    }
  }


  private final InputStream source;

  private byte[] cache;
  private int index;
  private int nextDataLength;


  private static SeqLanObjReader wrap(InputStream source) {
    return new SeqLanObjReader(source);
  }

  private SeqLanObjReader(InputStream source) {
    this.source = source;
    this.cache = new byte[32];
  }

  private <T> T read(Class<T> type, Field field) throws Exception {
    readNextDataLength();
    if (nextDataLength == 0) {
      return null;
    } else {
      int next = nextDataLength + index;
      T obj = readCurrentObject(type, field);
      index = next;
      return obj;
    }
  }

  @SuppressWarnings("unchecked")
  private <T> T readCurrentObject(Class<T> type, Field field) throws Exception {
    SeqLanDataType fieldType = SeqLanDataType.getClassType(type);
    switch (fieldType) {
      case OBJECT:
        return readMessage(type);
      case ARRAY: // unknown array nextDataLength, read as list first
        return (T) ReflectUtil.listToArray(readList(type.getComponentType()), type.getComponentType());
      case LIST:
        if (field == null) {
          throw new IllegalArgumentException("Type List<List<?>> is not supported now");
        }
        return (T) readList(ReflectUtil.getFieldGenericType(field, 0));
      case BIG_DECIMAL:
        return (T) readBigDecimal();
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
      case STRING:
        return (T) readString();
      default:
        throw new IllegalArgumentException("unsupported class: " + type.getSimpleName());
    }
  }

  // object and list
  private <T> T readMessage(Class<T> type) throws Exception {
    Field[] fields = type.getFields();
    T msg = type.newInstance();
    for (Field field : fields) {
      Object value = readMessageField((AbstractSeqLanObject) msg, field);
      if (value != null) {
        field.set(msg, value);
      }
    }
    return msg;
  }

  private Object readMessageField(AbstractSeqLanObject msg, Field field) throws Exception {
    for (Annotation annotation : field.getDeclaredAnnotations()) {
      if (annotation.annotationType().isAssignableFrom(ProcessFieldData.class)) {
        return processFieldData(msg, field);
      }
    }
    return read(field.getType(), field);
  }

  private Object processFieldData(AbstractSeqLanObject msg, Field field) throws Exception {
    readNextDataLength();
    readDataIntoCache();
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    SeqLan.writeLengthData(os, msg.processFieldData(cache, nextDataLength));
    return wrap(new ByteArrayInputStream(os.toByteArray())).read(field.getType(), field);
  }

  private <T> ArrayList<T> readList(Class<T> elementType) throws Exception {
    ArrayList<T> list = new ArrayList<>();
    int end = index + nextDataLength;
    while (end > index) {
      list.add(read(elementType, null));
    }
    return list;
  }

  private byte[] readRaw() throws IOException {
    byte[] data = new byte[nextDataLength];
    readData(data);
    return data;
  }

  private BigInteger readBigInteger() throws IOException {
    readDataIntoCache();
    byte[] bytes = new byte[nextDataLength];
    System.arraycopy(cache, 0, bytes, 0, nextDataLength);
    return new BigInteger(bytes);
  }

  private BigDecimal readBigDecimal() throws IOException {
    int totalLen = nextDataLength;
    readNextDataLength();
    BigInteger value = readBigInteger();
    nextDataLength = totalLen - SeqLan.varIntLength(nextDataLength) - nextDataLength;
    return new BigDecimal(value, readBigInteger().intValueExact());
  }

  private String readString() throws IOException {
    readDataIntoCache();
    return new String(cache, 0, nextDataLength);
  }

  private void readNextDataLength() throws IOException {
    nextDataLength = 0;
    int oneByte;
    do {
      oneByte = source.read();
      if (oneByte < 0) {
        throw new IOException("unexpected stream end reached");
      }
      ++index;
      nextDataLength = nextDataLength << 7;
      nextDataLength += oneByte & 0x7F;
    } while ((oneByte >>> 7) > 0);
  }

  private void readDataIntoCache() throws IOException {
    expandCache();
    readData(cache);
    index += nextDataLength;
  }

  private void readData(byte[] target) throws IOException {
    int read = source.read(target, 0, nextDataLength);
    if (read < 0) {
      throw new IOException("unexpected stream end reached");
    }
  }

  private void expandCache() {
    int cl = cache.length;
    while (cl < nextDataLength) {
      cl <<= 1;
      if (cl < 0) {
        cl = nextDataLength;
      }
    }
    cache = new byte[cl];
  }
}
