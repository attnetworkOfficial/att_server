package org.attnetwork.proto.sl;

import org.attnetwork.exception.AException;
import org.attnetwork.utils.BitmapFlags;
import org.attnetwork.utils.ReflectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "ConstantConditions"})
class SeqLanObjReader {
  private static final Logger log = LoggerFactory.getLogger(SeqLanObjReader.class);

  static <T extends AbstractSeqLanObject> T read(SeqLanObjReaderSource source, Class<T> msgType) {
    try {
      return (T) wrap(source).read(msgType);
    } catch (Exception e) {
      throw AException.wrap(e);
    }
  }


  private final SeqLanObjReaderSource source;

  private byte[] cache;
  private int index;
  private int nextDataLength;


  private static SeqLanObjReader wrap(SeqLanObjReaderSource source) {
    return new SeqLanObjReader(source);
  }

  private SeqLanObjReader(SeqLanObjReaderSource source) {
    this.source = source;
    this.cache = new byte[16];
  }


  private Object read(Type type) throws Exception {
    readNextDataLength();
    if (nextDataLength == 0) {
      return null;
    } else {
      int next = nextDataLength + index;
      Object obj = readCurrentObject(type);
      if (index < next) {
        log.warn("unread data remain for type:{}, index:{}, next:{}", type.getTypeName(), index, next);
        index = next;
      } else if (index > next) {
        throw new AException("read data exceeded data-end!");
      }
      return obj;
    }
  }

  private Object readCurrentObject(Type type) throws Exception {
    SeqLanDataType fieldType = SeqLanDataType.getTypeType(type);
    switch (fieldType) {
      case OBJECT:
        return readMessage((Class<?>) type);
      case ARRAY: // unknown array nextDataLength, read as list first
        Class<?> componentType = ((Class<?>) type).getComponentType();
        return ReflectUtil.listToArray(readList(null, componentType), componentType);
      case GENERIC_ARRAY:
        Type genericComponentType = ((GenericArrayType) type).getGenericComponentType();
        return ReflectUtil.listToArray(readList(null, genericComponentType), getParameterizedTypeClass(genericComponentType));
      case LIST:
        return readList(type);
      case MAP:
        return readMap(type);
      case BIG_DECIMAL:
        return readBigDecimal();
      case RAW:
        return readRaw();
      case BITMAP_FLAGS:
        return BitmapFlags.load((Class<Enum>) ReflectUtil.getGenericTypes(type)[0], readRaw());
      case INTEGER:
        return readBigInteger().intValueExact();
      case LONG:
        return readBigInteger().longValueExact();
      case BIG_INTEGER:
        return readBigInteger();
      case STRING:
        return readString();
      default:
        throw new IllegalArgumentException("read unsupported type: " + type.getTypeName());
    }
  }

  private Class<?> getParameterizedTypeClass(Type type) {
    return (Class<?>) ((ParameterizedType) type).getRawType();
  }

  private Object readMessage(Class<?> type) throws Exception {
    Field[] fields = type.getFields();
    Object msg = type.newInstance();
    for (Field field : fields) {
      Object value = read(field.getGenericType());
      if (value != null) {
        field.set(msg, value);
      }
    }
    return msg;
  }

  private List<?> readList(Type type) throws Exception {
    return readList(type, ReflectUtil.getGenericTypes(type)[0]);
  }

  private List<?> readList(Type type, Type elementType) throws Exception {
    List list;
    if (type == null) {
      list = ArrayList.class.newInstance();
    } else {
      Class<?> listType = getParameterizedTypeClass(type);
      list = listType.getName().equals("java.util.List")
          ? ArrayList.class.newInstance() : (List<?>) listType.newInstance();
    }
    int end = index + nextDataLength;
    while (end > index) {
      list.add(read(elementType));
    }
    return list;
  }

  private Object readMap(Type type) throws Exception {
    Class<?> mapType = getParameterizedTypeClass(type);
    Map map = mapType.getName().equals("java.util.Map")
        ? HashMap.class.newInstance() : (Map) mapType.newInstance();
    Type[] genericTypes = ReflectUtil.getGenericTypes(type);
    int end = index + nextDataLength;
    while (end > index) {
      map.put(read(genericTypes[0]), read(genericTypes[1]));
    }
    return map;
  }

  private byte[] readRaw() throws IOException {
    byte[] data = new byte[nextDataLength];
    readData(data);
    return data;
  }

  private BigInteger readBigInteger() throws IOException {
    if (nextDataLength == 0) {
      return BigInteger.ZERO;
    }
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
  }

  private void readData(byte[] target) throws IOException {
    int read = source.read(target, 0, nextDataLength);
    if (read < 0) {
      throw new IOException("unexpected source end reached");
    }
    index += nextDataLength;
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