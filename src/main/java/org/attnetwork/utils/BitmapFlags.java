package org.attnetwork.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BitmapFlags<E extends Enum<E>> {
  private byte[] flags;
  private final Class<E> enumType;


  @SafeVarargs
  public static <E extends Enum<E>> BitmapFlags<E> create(E... flags) {
    BitmapFlags<E> bf = create(flags[0].getDeclaringClass());
    for (E ordinal : flags) {
      bf.set(ordinal, true);
    }
    return bf;
  }

  public static <E extends Enum<E>> BitmapFlags<E> create(Class<E> enumType) {
    return load(enumType, null);
  }

  public static <E extends Enum<E>> BitmapFlags<E> load(Class<E> enumType, byte[] flags) {
    return new BitmapFlags<>(enumType, flags);
  }

  private BitmapFlags(Class<E> enumType, byte[] flags) {
    this.enumType = enumType;
    this.flags = flags;
  }

  public Boolean hasAll(E... enums) {
    for (E e : enums) {
      if (!has(e)) {
        return false;
      }
    }
    return true;
  }

  public Boolean has(E e) {
    if (flags == null) {
      return false;
    }
    int l = e.ordinal();
    int i = l / 4;
    return flags.length > i && ((flags[i] >> (l % 4)) & 1) == 1;
  }

  public void set(E ordinal, boolean flag) {
    int l = ordinal.ordinal();
    int i = l / 4;
    if (flags == null) {
      flags = new byte[i + 1];
    } else if (flags.length <= i) {
      flags = Arrays.copyOf(flags, i + 1);
    }
    int w = 1 << (l % 4);
    flags[i] = (byte) (flag ? (flags[i] | w) : (flags[i] & ~w));
  }

  public byte[] getFlags() {
    return flags;
  }

  public List<E> typeInList() {
    List<E> list = new ArrayList<>();
    for (E e : enumType.getEnumConstants()) {
      if (this.has(e)) {
        list.add(e);
      }
    }
    return list;
  }
}
