package org.attnetwork.server.component.impl.l2;

import java.util.concurrent.ConcurrentHashMap;
import org.attnetwork.server.AtTnSession;
import org.attnetwork.server.component.l2.SessionServiceL2;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceL2Impl implements SessionServiceL2 {

  private ConcurrentHashMap<String, AtTnSession> sessionMap = new ConcurrentHashMap<>();

  @Override
  public AtTnSession getSession(String sessionId) {
    return sessionMap.get(sessionId);
  }
}
