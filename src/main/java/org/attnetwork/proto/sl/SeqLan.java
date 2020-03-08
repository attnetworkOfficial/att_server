package org.attnetwork.proto.sl;

import java.io.IOException;
import java.io.OutputStream;

public class SeqLan {
  public static final String RAW = "raw";
  public static final String NUMBER = "number";
  public static final String DECIMAL = "decimal";
  public static final String STRING = "string";
  public static final String ARRAY = "array";
  public static final String OBJECT = "object";

  static void writeVarInt(OutputStream os, int varInt) throws IOException {
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

  static int varIntLength(int varInt) {
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

  static void writeLengthData(OutputStream os, byte[] data) throws IOException {
    if (data == null || data.length == 0) {
      os.write(0);
    } else {
      SeqLan.writeVarInt(os, data.length);
      os.write(data);
    }
  }
}
