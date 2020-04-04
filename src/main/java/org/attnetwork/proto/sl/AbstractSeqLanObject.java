package org.attnetwork.proto.sl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.attnetwork.exception.AException;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.bouncycastle.util.encoders.Base64;

public abstract class AbstractSeqLanObject {
  private int dataLengthLen;
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

  void writeWithoutLen(OutputStream os) throws IOException {
    os.write(getRaw(), dataLengthLen, raw.length - dataLengthLen);
  }

  public byte[] getRaw() {
    if (raw == null) {
      createRaw();
    }
    return raw;
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


  private void createRaw() {
    try {
      byte[] data = SeqLanObjWriter.toByteArray(this);
      if (data.length > 0) {
        dataLengthLen = SeqLan.varIntLength(data.length);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        SeqLan.writeLengthData(os, data);
        raw = os.toByteArray();
      } else {
        dataLengthLen = 0;
        raw = data;
      }
    } catch (Exception e) {
      throw AException.wrap(e);
    }
  }
}