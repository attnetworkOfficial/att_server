package org.attnetwork.server.component.l2.impl;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.attnetwork.crypto.asymmetric.AsmPublicKey;
import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.exception.AException;
import org.attnetwork.proto.msg.SessionStartMsg;
import org.attnetwork.proto.msg.SessionStartMsgResp;
import org.attnetwork.server.component.l2.SessionServiceL2;
import org.attnetwork.server.component.l2.obj.AtTnSession;
import org.attnetwork.utils.HashUtil;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceL2Impl implements SessionServiceL2 {
  private static final int SESSION_LIMIT = 2;
  private ConcurrentHashMap<AsmPublicKey, List<AtTnSession>> userToSessionIdMap = new ConcurrentHashMap<>();
  private ConcurrentHashMap<Integer, AtTnSession> sessionMap = new ConcurrentHashMap<>();

  private SecureRandom secureRandom = new SecureRandom();

  @Override
  public SessionStartMsgResp startSession(SessionStartMsg req, AsmPublicKeyChain signer) {
    if (req.random == null || req.random.length < 128) {
      throw new AException("client's random is too short");
    }
    byte[] serverRandom = new byte[128];
    secureRandom.nextBytes(serverRandom);
    byte[] sharedSecret = HashUtil.sha512(req.random, serverRandom);
    AtTnSession session = createSession(sharedSecret, signer);
    saveUserToSessionIdMap(session, signer);
    int sessionId = generateSessionIdAndSave(session);
    return createStartSessionResp(serverRandom, sessionId);
  }

  private AtTnSession createSession(byte[] sharedSecret, AsmPublicKeyChain publicKeyChain) {
    AtTnSession session = new AtTnSession();
    session.algorithm = "default";
    session.sharedSecret = sharedSecret;
    session.userPublicKeyChain = publicKeyChain;
    session.createTimestamp = System.currentTimeMillis();
    session.lastActiveTime = System.currentTimeMillis();
    return session;
  }

  private int generateSessionIdAndSave(AtTnSession session) {
    while (true) {
      int sessionId = secureRandom.nextInt();
      AtTnSession old = sessionMap.put(sessionId, session);
      if (old == null) {
        session.sessionId = sessionId;
        return sessionId;
      }
      // if sessionId collides with an old one, regenerate it;
      sessionMap.put(sessionId, old); // put it back
    }
  }

  private void saveUserToSessionIdMap(AtTnSession session, AsmPublicKeyChain publicKeyChain) {
    AsmPublicKey userRootKey = publicKeyChain.rootKey();
    List<AtTnSession> list = userToSessionIdMap.computeIfAbsent(userRootKey, k -> new ArrayList<>());
    synchronized (list) {
      if (list.size() >= SESSION_LIMIT) {
        throw new AException("exceed session limit");
      }
      list.add(session);
    }
  }

  private SessionStartMsgResp createStartSessionResp(byte[] serverRandom, int sessionId) {
    SessionStartMsgResp resp = new SessionStartMsgResp();
    resp.algorithm = "default";
    resp.random = serverRandom;
    resp.sessionId = sessionId;
    return resp;
  }
}
