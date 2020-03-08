package org.attnetwork.proto.sl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.slf4j.LoggerFactory;

public abstract class AbstractSeqLanObject {
  protected byte[] raw;

  /**
   * convert raw message to a Java Object instance.
   */
  public static <T extends AbstractSeqLanObject> T read(InputStream source, Class<T> msgType) throws IOException {
    return SeqLanObjReader.read(source, msgType);
  }

  /**
   * write message into an output stream.
   */
  public void write(OutputStream os) throws IOException {
    SeqLanObjWriter.writeLengthData(os, getRaw());
  }

  public byte[] getRaw() {
    return raw == null ? getNewRaw() : raw;
  }

  public byte[] getNewRaw() {
    return raw = SeqLanObjWriter.toByteArray(this);
  }

  protected void beforeReadDo(int fieldIndex) {
    LoggerFactory.getLogger(getClass()).warn("before read field {} do nothing", fieldIndex);
  }
}