package org.attnetwork.proto.sl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSeqLanObject {
  private Logger log = LoggerFactory.getLogger(getClass());

  byte[] raw;

  /**
   * convert raw message to a Java Object instance.
   */
  public static <T extends AbstractSeqLanObject> T read(InputStream source, Class<T> msgType) {
    return SeqLanObjReader.read(source, msgType);
  }

  public static <T extends AbstractSeqLanObject> T read(byte[] source, Class<T> msgType) {
    return read(new ByteArrayInputStream(source), msgType);
  }

  public static <T extends AbstractSeqLanObject> T readBase64String(String source, Class<T> msgType) {
    return read(Base64.decode(source), msgType);
  }

  public static <T extends AbstractSeqLanObject> T readHexString(String source, Class<T> msgType) {
    return read(ByteUtils.fromHexString(source), msgType);
  }

  /**
   * write message into an output stream.
   */
  public void write(OutputStream os) throws IOException {
    os.write(getRaw());
  }

  public byte[] getRaw() {
    return raw == null ? raw = SeqLanObjWriter.toByteArray(this) : raw;
  }

  public void clearRaw() {
    raw = null;
  }

  public void setRaw(byte[] raw) {
    this.raw = raw;
  }

  public String toBase64String() {
    return Base64.toBase64String(getRaw());
  }

  public String toHexString() {
    return ByteUtils.toHexString(getRaw());
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