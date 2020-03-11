package org.attnetwork.server.component.l2.impl;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import org.attnetwork.proto.msg.SessionStartMsg;
import org.attnetwork.server.component.l2.obj.AtTnSession;
import org.attnetwork.server.component.l2.SessionServiceL2;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceL2Impl implements SessionServiceL2 {

  private ConcurrentHashMap<Integer, AtTnSession> sessionMap = new ConcurrentHashMap<>();

  @Override
  public AtTnSession getSession(byte[] sessionId) {
    return sessionMap.get(new BigInteger(sessionId).intValue());
  }

  public void startSession(SessionStartMsg msg) {

  }
}
