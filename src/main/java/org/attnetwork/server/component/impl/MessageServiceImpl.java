package org.attnetwork.server.component.impl;

import static org.attnetwork.proto.sl.AbstractSeqLanObject.read;

import java.io.InputStream;
import java.io.OutputStream;
import org.attnetwork.crypto.EncryptedData;
import org.attnetwork.exception.AException;
import org.attnetwork.proto.msg.wrapper.AtTnMsg;
import org.attnetwork.proto.msg.wrapper.SignedMsg;
import org.attnetwork.proto.msg.wrapper.WrapType;
import org.attnetwork.proto.msg.wrapper.WrappedMsg;
import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.attnetwork.server.component.MessageProcess;
import org.attnetwork.server.component.MessageService;
import org.attnetwork.server.component.l2.EncryptServiceL2;
import org.attnetwork.server.component.l2.SessionServiceL2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {
  private final SessionServiceL2 sessionService;
  private final EncryptServiceL2 encryptService;

  @Autowired
  public MessageServiceImpl(SessionServiceL2 sessionService, EncryptServiceL2 encryptService) {
    this.sessionService = sessionService;
    this.encryptService = encryptService;
  }

  @Override
  public void process(InputStream is, OutputStream os) {
    MessageProcess process = unwrap(is);
    switch (process.getMsgType()) {
      case "session-start":
        break;
    }
  }

  private MessageProcess unwrap(InputStream is) {
    MessageProcess process = new MessageProcess();
    process.wrappedMsg = read(is, WrappedMsg.class);
    while (process.needUnwrap()) {
      switch (process.getWrapType()) {
        case ATTN_PROTO:
          unwrapAtTnProto(process);
          break;
        case SIGN:
          checkSignedMsg(process);
          break;
        case ENCRYPT:
          decryptMsg(process);
          break;
      }
    }
    return process.doFinal();
  }

  private void unwrapAtTnProto(MessageProcess process) {
    AtTnMsg atTnMsg = process.unwrapMsg(AtTnMsg.class);
    // TODO decode ATTN_Proto data
    byte[] decrypted = atTnMsg.data;

    process.sessionId = atTnMsg.sessionId;
    process.loadWrappedData(decrypted);
  }

  private void checkSignedMsg(MessageProcess process) {
    SignedMsg signedMsg = process.unwrapMsg(SignedMsg.class);
//    TODO trusted key check
//    encryptService.ecVerifyPublicKeyChain(signedMsg.publicKeyChain, trusted);

    boolean validSign = encryptService.ecVerify(
        signedMsg.publicKeyChain.key, signedMsg.sign, signedMsg.data);
    if (!validSign) {
      throw new AException("sign error");
    }
    process.addSigner(signedMsg.publicKeyChain);
    process.loadWrappedData(signedMsg.data);
  }

  private void decryptMsg(MessageProcess process) {
    EncryptedData encryptedData = process.unwrapMsg(EncryptedData.class);
    byte[] decrypted = encryptService.ecDecrypt(encryptedData.data);
    process.loadWrappedData(decrypted);
  }

  private WrappedMsg sign(AbstractSeqLanObject msg) {
    SignedMsg signedMsg = new SignedMsg();
    byte[] data = msg.getRaw();
    signedMsg.data = data;
    signedMsg.sign = encryptService.ecSign(data);
    signedMsg.publicKeyChain = encryptService.getPublicKeyChain();
    return WrappedMsg.wrap(WrapType.SIGN, signedMsg.getRaw());
  }
}
