package org.attnetwork.server.component.l2;

import org.attnetwork.server.component.l2.obj.AtTnSession;

public interface SessionServiceL2 {
  AtTnSession getSession(byte[] sessionId);
}
