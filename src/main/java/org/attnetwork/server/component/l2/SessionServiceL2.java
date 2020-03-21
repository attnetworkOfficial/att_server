package org.attnetwork.server.component.l2;

import org.attnetwork.proto.msg.SessionStartMsgResp;
import org.attnetwork.proto.msg.wrapper.AtTnEncryptedMsg;
import org.attnetwork.proto.msg.wrapper.AtTnOriginMsg;
import org.attnetwork.server.component.MessageOnion;

public interface SessionServiceL2 {
  SessionStartMsgResp startSession(MessageOnion onion);

  AtTnEncryptedMsg atTnEncrypt(byte[] data, Integer sessionId);

  AtTnOriginMsg atTnDecrypt(AtTnEncryptedMsg encryptedMsg);
}
