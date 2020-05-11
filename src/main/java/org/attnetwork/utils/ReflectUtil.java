package org.attnetwork.utils;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;


@SuppressWarnings("unchecked")
public class ReflectUtil {
  public static <T> T[] listToArray(List<T> list, Class<?> type) {
    return list.toArray((T[]) Array.newInstance(type, list.size()));
  }

  public static Type[] getGenericTypes(Type type) {
    if (type == null) {
      throw new IllegalArgumentException();
    }
    if (type instanceof ParameterizedType) {
      return ((ParameterizedType) type).getActualTypeArguments();
    } else {
      return null;
    }
  }
}
