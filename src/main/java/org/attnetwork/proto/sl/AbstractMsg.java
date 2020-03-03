package org.attnetwork.proto.sl;

import java.io.OutputStream;

public abstract class AbstractMsg {
  /**
   * convert raw message to a Java Object instance.
   */
  public static <T extends AbstractMsg> T read(byte[] raw, Class<T> msgType) {
    return Reader.read(raw, msgType);
  }

  /**
   * write message into an output stream.
   */
  public void write(OutputStream os) {
    Writer.writeMessage(os, this);
  }
}