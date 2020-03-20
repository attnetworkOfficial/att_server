package org.attnetwork.server.component.l2.impl;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.attnetwork.crypto.asymmetric.AsmPublicKey;
import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.exception.AException;
import org.attnetwork.proto.attn.AttnProto;
import org.attnetwork.proto.msg.SessionStartMsg;
import org.attnetwork.proto.msg.SessionStartMsgResp;
import org.attnetwork.server.component.MessageOnion;
import org.attnetwork.server.component.l2.SessionServiceL2;
import org.attnetwork.server.component.l2.obj.AtTnSession;
import org.attnetwork.utils.HashUtil;
import org.springframework.stereotype.Service;

@Service
public class SessionServiceL2Impl implements SessionServiceL2 {
  private static final int SESSION_LIMIT = 2;
  private final ConcurrentHashMap<AsmPublicKey, List<AtTnSession>> userToSessionIdMap = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Integer, AtTnSession> sessionMap = new ConcurrentHashMap<>();
  private final SecureRandom secureRandom = new SecureRandom();

  @Override
  public void startSession(MessageOnion onion) {
    SessionStartMsg msg = onion.readTypedMsg(SessionStartMsg.class);
    AttnProto proto = AttnProto.getByVersion(msg.attnVersion);
    if (proto == null) {
      throw new AException("unsupported ATTN protocol version: " + msg.attnVersion);
    }
    if (msg.random == null || msg.random.length < proto.SESSION_START_RANDOM_SIZE) {
      throw new AException("client's random is too short");
    }
    byte[] serverRandom = randomBytes(proto.SESSION_START_RANDOM_SIZE);
    byte[] sharedSecret = HashUtil.sha512(msg.random, serverRandom);
    AtTnSession session = createSession(proto, sharedSecret, onion.getSigner());
    saveUserToSessionIdMap(session);
    generateSessionIdAndSave(session);

    onion.revive(createStartSessionResp(serverRandom, session));
  }

  private AtTnSession createSession(AttnProto proto, byte[] sharedSecret, AsmPublicKeyChain publicKeyChain) {
    AtTnSession session = new AtTnSession();
    session.proto = proto;
    session.sharedSecret = sharedSecret;
    session.userPublicKeyChain = publicKeyChain;
    session.createTimestamp = System.currentTimeMillis();
    session.lastActiveTime = System.currentTimeMillis();
    session.saltTimestamp = 0L;
    generateSessionSalt(session);
    return session;
  }

  private void generateSessionSalt(AtTnSession session) {
    long now = System.currentTimeMillis();
    if (session.saltTimestamp < now) {
      byte[] newSalt = randomBytes(session.proto.SERVER_SALT_SIZE);
      session.oldSalt = session.salt;
      session.salt = newSalt;
      session.saltTimestamp = now + session.proto.SERVER_SALT_UPDATE_TIME;
    }
  }

  private void generateSessionIdAndSave(AtTnSession session) {
    while (true) {
      int sessionId = secureRandom.nextInt();
      AtTnSession old = sessionMap.put(sessionId, session);
      if (old == null) {
        session.id = sessionId;
        return;
      }
      // if sessionId collides with an old one, regenerate it;
      sessionMap.put(sessionId, old); // put it back
    }
  }

  private void saveUserToSessionIdMap(AtTnSession session) {
    AsmPublicKey userRootKey = session.userPublicKeyChain.rootKey();
    List<AtTnSession> list = userToSessionIdMap.computeIfAbsent(userRootKey, k -> new ArrayList<>());
    synchronized (list) {
      if (list.size() >= SESSION_LIMIT) {
        throw new AException("exceed session limit");
      }
      list.add(session);
    }
  }

  private SessionStartMsgResp createStartSessionResp(byte[] serverRandom, AtTnSession session) {
    SessionStartMsgResp resp = new SessionStartMsgResp();
    resp.version = "default";
    resp.sessionId = session.id;
    resp.random = serverRandom;
    resp.salt = session.salt;
    return resp;
  }


  private byte[] randomBytes(int l) {
    byte[] bytes = new byte[l];
    secureRandom.nextBytes(bytes);
    return bytes;
  }
}
