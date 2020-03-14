package org.attnetwork.server.component.l2;

import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.proto.msg.SessionStartMsg;
import org.attnetwork.proto.msg.SessionStartMsgResp;

public interface SessionServiceL2 {
  SessionStartMsgResp startSession(SessionStartMsg req, AsmPublicKeyChain signer);
}
