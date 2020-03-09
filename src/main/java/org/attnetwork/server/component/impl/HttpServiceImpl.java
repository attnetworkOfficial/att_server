package org.attnetwork.server.component.impl;

import java.io.IOException;
import org.attnetwork.proto.msg.SessionStartMsg;
import org.attnetwork.proto.msg.wrapper.AtTnMsg;
import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.attnetwork.server.AtTnSession;
import org.attnetwork.server.component.HttpService;
import org.attnetwork.server.component.l2.SessionServiceL2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpInputMessage;
import org.springframework.stereotype.Service;

@Service
public class HttpServiceImpl implements HttpService {
  private final SessionServiceL2 sessionService;

  @Autowired
  public HttpServiceImpl(SessionServiceL2 sessionService) {
    this.sessionService = sessionService;
  }

  @Override
  public AbstractSeqLanObject readInternal(Class<? extends AbstractSeqLanObject> type, HttpInputMessage inputMessage) throws IOException {
    if (SessionStartMsg.class.isAssignableFrom(type)) {

    }
    AtTnMsg msg = AtTnMsg.read(inputMessage.getBody(), AtTnMsg.class);
    AtTnSession session = sessionService.getSession(msg.sessionId);
    byte[] data = msg.getMsg();
    return AtTnMsg.read(inputMessage.getBody(), type);
  }
}
