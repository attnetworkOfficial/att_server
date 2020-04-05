package org.attnetwork.proto.sl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public abstract class SeqLanObjReaderSource {
  public static SeqLanObjReaderSource wrap(InputStream source) {
    return new WrapInputStream(source);
  }

  public static SeqLanObjReaderSource wrap(ByteBuffer source) {
    return new WrapByteBuffer(source);
  }

  abstract int read() throws IOException;

  abstract int read(byte[] src, int offset, int length) throws IOException;
}

class WrapInputStream extends SeqLanObjReaderSource {
  private final InputStream source;

  WrapInputStream(InputStream source) {
    this.source = source;
  }

  @Override
  int read() throws IOException {
    return source.read();
  }

  @Override
  int read(byte[] src, int offset, int length) throws IOException {
    return source.read(src, offset, length);
  }
}

class WrapByteBuffer extends SeqLanObjReaderSource {
  private final ByteBuffer source;

  WrapByteBuffer(ByteBuffer source) {
    this.source = source;
  }

  @Override
  public int read() {
    return source.get();
  }

  @Override
  public int read(byte[] src, int offset, int length) {
    if (source.remaining() < length) {
      return -1;
    } else {
      source.get(src, offset, length);
      return length;
    }
  }
}
