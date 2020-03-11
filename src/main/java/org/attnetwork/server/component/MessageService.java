package org.attnetwork.server.component;

import java.io.InputStream;
import java.io.OutputStream;

public interface MessageService {
  void process(InputStream is, OutputStream os);
}
