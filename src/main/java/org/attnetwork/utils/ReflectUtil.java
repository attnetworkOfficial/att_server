package org.attnetwork.utils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;


@SuppressWarnings("unchecked")
public class ReflectUtil {
  public static <T> T[] listToArray(List<T> list, Class<?> type) {
    return list.toArray((T[]) Array.newInstance(type, list.size()));
  }

  public static Class<?> getFieldGenericType(Field field, int i) {
    if (field == null) {
      throw new IllegalArgumentException();
    }
    ParameterizedType genericType = (ParameterizedType) field.getGenericType();
    return (Class<?>) genericType.getActualTypeArguments()[i];
  }
}
