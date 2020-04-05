package org.attnetwork.server.component;

import java.io.InputStream;
import java.io.OutputStream;
import org.attnetwork.proto.sl.SeqLanObjReaderSource;
import org.attnetwork.proto.sl.SeqLanObjWriterTarget;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

public interface MessageService {
  default void process(InputStream source, OutputStream os) {
    process(SeqLanObjReaderSource.wrap(source), SeqLanObjWriterTarget.wrap(os));
  }

  void process(SeqLanObjReaderSource source, SeqLanObjWriterTarget target);

  void processWebSocket(WebSocketSession session, BinaryMessage message, OutputStream os);

  void wrap(MessageOnion onion);

  void unwrap(MessageOnion process);
}
