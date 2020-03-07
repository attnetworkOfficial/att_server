package org.attnetwork.proto.sl;

import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractSeqLanObject {
  byte[] raw;

  /**
   * convert raw message to a Java Object instance.
   */
  public static <T extends AbstractSeqLanObject> T read(byte[] raw, Class<T> msgType) {
    return read(raw, msgType, Integer.MAX_VALUE);
  }

  public static <T extends AbstractSeqLanObject> T read(byte[] raw, Class<T> msgType, int readFieldLimits) {
    return SeqLanObjReader.read(raw, msgType, readFieldLimits);
  }

  /**
   * write message into an output stream.
   */
  public void write(OutputStream os) throws IOException {
    SeqLanObjWriter.writeLengthData(os, getRaw(this));
  }

  protected static byte[] getRaw(AbstractSeqLanObject msg) {
    return msg == null ? new byte[]{0} : SeqLanObjWriter.toByteArray(msg);
  }
}