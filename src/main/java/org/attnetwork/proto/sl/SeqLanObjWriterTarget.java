package org.attnetwork.proto.sl;

import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.OutputStream;

public abstract class SeqLanObjWriterTarget {
  public static SeqLanObjWriterTarget wrap(OutputStream target) {
    return new WrapOutPutStream(target);
  }

  public static SeqLanObjWriterTarget wrap(WebSocketSession target) {
    return new WrapWebSocketSession(target);
  }

  public void write(byte[] src) throws IOException {
    write(src, 0, src.length);
  }

  public abstract void write(byte[] src, int offset, int length) throws IOException;

  public abstract void flush() throws IOException;
}

class WrapWebSocketSession extends SeqLanObjWriterTarget {
  private final WebSocketSession session;

  WrapWebSocketSession(WebSocketSession session) {
    this.session = session;
  }

  @Override
  public void write(byte[] src, int offset, int length) throws IOException {
    session.sendMessage(new BinaryMessage(src, offset, length, true));
  }

  @Override
  public void flush() {
  }
}

class WrapOutPutStream extends SeqLanObjWriterTarget {
  private final OutputStream target;

  WrapOutPutStream(OutputStream target) {
    this.target = target;
  }

  @Override
  public void write(byte[] src, int offset, int length) throws IOException {
    target.write(src, offset, length);
  }

  @Override
  public void flush() throws IOException {
    target.flush();
  }
}
