package org.attnetwork.proto.sl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSeqLanObject {
  protected Logger log = LoggerFactory.getLogger(getClass());

  protected byte[] raw;

  /**
   * convert raw message to a Java Object instance.
   */
  public static <T extends AbstractSeqLanObject> T read(InputStream source, Class<T> msgType) {
    return SeqLanObjReader.read(source, msgType);
  }

  /**
   * write message into an output stream.
   */
  public void write(OutputStream os) throws IOException {
    SeqLan.writeLengthData(os, getRaw());
  }

  public byte[] getRaw() {
    return raw == null ? SeqLanObjWriter.toByteArray(this) : raw;
  }

  public void clearRaw() {
    raw = null;
  }

  public void setRaw(byte[] raw) {
    this.raw = raw;
  }

  @Target(ElementType.FIELD)
  @Retention(RetentionPolicy.RUNTIME)
  protected @interface ProcessFieldData {
  }

  protected byte[] processFieldData(byte[] cache, int dataLength) {
    log.info("{}, process field data did nothing", getClass().getName());
    byte[] processedData = new byte[dataLength];
    System.arraycopy(cache, 0, processedData, 0, dataLength);
    return processedData;
  }
}