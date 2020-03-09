package org.attnetwork.server.component.l2;

import org.attnetwork.server.AtTnSession;

public interface SessionServiceL2 {
  AtTnSession getSession(String sessionId);
}
