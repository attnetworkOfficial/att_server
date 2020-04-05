package org.attnetwork.server.component.l2.impl;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.attnetwork.crypto.asymmetric.AsmPublicKey;
import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.exception.AException;
import org.attnetwork.proto.attn.AtTnProto;
import org.attnetwork.proto.msg.SessionStartMsg;
import org.attnetwork.proto.msg.SessionStartMsgResp;
import org.attnetwork.proto.msg.wrapper.AtTnEncryptedMsg;
import org.attnetwork.proto.msg.wrapper.AtTnOriginMsg;
import org.attnetwork.server.component.MessageOnion;
import org.attnetwork.server.component.l2.SessionServiceL2;
import org.attnetwork.server.component.l2.obj.AtTnSession;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceL2Impl implements SessionServiceL2 {
  private static final int SESSION_LIMIT = 2;
  private final ConcurrentHashMap<AsmPublicKey, List<AtTnSession>> userToSessionIdMap = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Integer, AtTnSession> sessionMap = new ConcurrentHashMap<>();
  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public SessionStartMsgResp startSession(MessageOnion onion) {
    SessionStartMsg msg = onion.readTypedMsg(SessionStartMsg.class);
    AtTnProto proto = AtTnProto.getByVersion(msg.attnVersion);
    if (proto == null) {
      throw new AException("unsupported ATTN protocol version: " + msg.attnVersion);
    }
    if (msg.random == null || msg.random.length < proto.SESSION_START_RANDOM_SIZE) {
      throw new AException("client's random is too short");
    }
    byte[] serverRandom = randomBytes(proto.SESSION_START_RANDOM_SIZE);
    AtTnSession session = createSession(proto, msg.random, serverRandom, onion.getSigner());

    return createStartSessionResp(serverRandom, session);
  }

  @Override
  public AtTnEncryptedMsg atTnEncrypt(byte[] data, Integer sessionId) {
    return getSession(sessionId).encrypt(data);
  }

  @Override
  public AtTnOriginMsg atTnDecrypt(AtTnEncryptedMsg encryptedMsg) {
    return getSession(encryptedMsg.sessionId).decrypt(encryptedMsg);
  }

//  @Override
  public void checkWebSocketSession() {

  }



  private AtTnSession getSession(Integer id) {
    if (id == null) {
      throw new AException("session id is null");
    }
    AtTnSession session = sessionMap.get(id);
    if (session == null) {
      throw new AException("session not exists, id:" + id);
    }
    return session;
  }

  private AtTnSession createSession(AtTnProto proto, byte[] clientRandom, byte[] serverRandom, AsmPublicKeyChain publicKeyChain) {
    List<AtTnSession> list = userToSessionIdMap.computeIfAbsent(publicKeyChain.rootKey(), k -> new ArrayList<>());
    AtTnSession session = new AtTnSession();
    synchronized (list) {
      if (list.size() >= SESSION_LIMIT) {
        throw new AException("exceed session limit");
      }
      list.add(session);
    }
    int sessionId = generateSessionIdAndSave(session);
    session.init(sessionId, proto, clientRandom, serverRandom, publicKeyChain);
    return session;
  }

  private int generateSessionIdAndSave(AtTnSession session) {
    while (true) {
      int sessionId = secureRandom.nextInt();
      AtTnSession old = sessionMap.put(sessionId, session);
      if (old == null) {
        return sessionId;
      }
      // if sessionId collides with an old one, regenerate it;
      sessionMap.put(sessionId, old); // put it back
    }
  }


  private SessionStartMsgResp createStartSessionResp(byte[] serverRandom, AtTnSession session) {
    SessionStartMsgResp resp = new SessionStartMsgResp();
    resp.version = session.getProto().VERSION;
    resp.sessionId = session.getId();
    resp.random = serverRandom;
    resp.salt = session.getSalt();
    return resp;
  }


  private byte[] randomBytes(int l) {
    byte[] bytes = new byte[l];
    secureRandom.nextBytes(bytes);
    return bytes;
  }
}
