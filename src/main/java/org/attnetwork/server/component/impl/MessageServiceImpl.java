package org.attnetwork.server.component.impl;

import org.attnetwork.crypto.EncryptedData;
import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.exception.AException;
import org.attnetwork.proto.msg.ErrorMsg;
import org.attnetwork.proto.msg.wrapper.AtTnEncryptedMsg;
import org.attnetwork.proto.msg.wrapper.AtTnOriginMsg;
import org.attnetwork.proto.msg.wrapper.SignedMsg;
import org.attnetwork.proto.msg.wrapper.WrapType;
import org.attnetwork.proto.sl.SeqLanObjReaderSource;
import org.attnetwork.proto.sl.SeqLanObjWriterTarget;
import org.attnetwork.server.component.MessageOnion;
import org.attnetwork.server.component.MessageService;
import org.attnetwork.server.component.MessageType;
import org.attnetwork.server.component.l2.EncryptServiceL2;
import org.attnetwork.server.component.l2.SessionServiceL2;
import org.attnetwork.server.component.l2.UserServiceL2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.OutputStream;

@Service
public class MessageServiceImpl implements MessageService {

  private final SessionServiceL2 sessionService;
  private final EncryptServiceL2 encryptService;
  private final UserServiceL2 userServiceL2;
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  public MessageServiceImpl(SessionServiceL2 sessionService, EncryptServiceL2 encryptService, UserServiceL2 userServiceL2) {
    this.sessionService = sessionService;
    this.encryptService = encryptService;
    this.userServiceL2 = userServiceL2;
  }

  @Override
  public void process(SeqLanObjReaderSource source, SeqLanObjWriterTarget target) {
    try {
      MessageOnion onion = MessageOnion.sow(source);
      unwrap(onion);
      String msgType = onion.getMsgType();
      switch (msgType) {
        case MessageType.__DEBUG__:
          __debug__(onion);
          break;
        case MessageType.START_SESSION:
          startSession(onion);
          break;
        case MessageType.PING:
          target.write(new byte[]{0});
          return;
        case MessageType.QUERY_CHATS:
          break;
        case "null":
        default:
          throw new AException("unsupported message type: " + msgType);
      }
      wrap(onion);
      onion.harvest(target);
    } catch (Exception e) {
      log.error("", e);
      exceptionHandle(e, target);
    }
  }

  @Override
  public void processWebSocket(WebSocketSession session, BinaryMessage message, OutputStream os) {
  }

  private void startSession(MessageOnion onion) {
    onion.checkWrapTypes(WrapType.L_SIGN_ENCRYPT);
    userServiceL2.validUserCheck(onion.getSigner());
    onion.revive(sessionService.startSession(onion));
    onion.setWrapTypes(WrapType.L_ENCRYPT_SIGN);
  }

  private void __debug__(MessageOnion onion) {
    onion.checkWrapTypes(WrapType.L_ATTN_PROTO);
    onion.revive(MessageType.__DEBUG__, null);
    onion.setWrapTypes(WrapType.L_ATTN_PROTO);
  }

  private void exceptionHandle(Exception e, SeqLanObjWriterTarget target) {
    try {
      ErrorMsg errorMsg = new ErrorMsg();
      errorMsg.code = e instanceof AException ? ((AException) e).getCode() : "400";
      errorMsg.msg = e.getMessage();
      MessageOnion.sow("resp.error", errorMsg).harvest(target);
    } catch (IOException ioE) {
      log.error("write outputStream error, {}", ioE.getMessage());
    }
  }

  @Override
  public void wrap(MessageOnion onion) {
    for (WrapType wrapType : onion.getWrapTypes()) {
      switch (wrapType) {
        case ATTN_PROTO:
          wrapAtTnProto(onion);
          break;
        case SIGN:
          sign(onion);
          break;
        case ENCRYPT:
          encrypt(onion);
          break;
      }
    }
  }

  @Override
  public void unwrap(MessageOnion onion) {
    WrapType wrapType;
    while ((wrapType = onion.getWrapType()) != null) {
      switch (wrapType) {
        case ATTN_PROTO:
          unwrapAtTnProto(onion);
          break;
        case SIGN:
          checkSign(onion);
          break;
        case ENCRYPT:
          decrypt(onion);
          break;
      }
    }
    onion.readTypedMsgFromWrappedMsg();
  }

  private void wrapAtTnProto(MessageOnion onion) {
    byte[] data = onion.getProcessingMsg().getRaw();
    AtTnEncryptedMsg encryptedMsg = sessionService.atTnEncrypt(data, onion.getSessionId());

    onion.wrap(WrapType.ATTN_PROTO, encryptedMsg);
  }

  private void unwrapAtTnProto(MessageOnion onion) {
    AtTnEncryptedMsg atTnEncryptedMsg = onion.unwrapMsg(AtTnEncryptedMsg.class);
    AtTnOriginMsg originMsg = sessionService.atTnDecrypt(atTnEncryptedMsg);

    onion.setSessionId(atTnEncryptedMsg.sessionId);
    onion.loadWrappedData(originMsg.data);
  }

  // ----------------------------------------------------------------------------------------
  private void sign(MessageOnion onion) {
    byte[] data = onion.getProcessingMsg().getRaw();
    SignedMsg signedMsg = new SignedMsg();
    signedMsg.data = data;
    signedMsg.sign = encryptService.ecSign(data);
    signedMsg.publicKeyChain = encryptService.getPublicKeyChain();

    onion.wrap(WrapType.SIGN, signedMsg);
  }

  private void checkSign(MessageOnion onion) {
    SignedMsg signedMsg = onion.unwrapMsg(SignedMsg.class);
    AsmPublicKeyChain.Validation validation = encryptService.ecVerifyPublicKeyChain(
        signedMsg.publicKeyChain, null);
    if (!validation.isValid) {
      throw new AException("public key-chain invalid");
    }
    boolean validSign = encryptService.ecVerify(
        signedMsg.publicKeyChain.key, signedMsg.sign, signedMsg.data);
    if (!validSign) {
      throw new AException("message sign error");
    }

    onion.setSigner(signedMsg.publicKeyChain).loadWrappedData(signedMsg.data);
  }

  // ----------------------------------------------------------------------------------------
  private void encrypt(MessageOnion onion) {
    byte[] data = onion.getProcessingMsg().getRaw();
    EncryptedData encryptedData = encryptService.ecEncrypt(onion.getSigner().key, data);

    onion.wrap(WrapType.ENCRYPT, encryptedData);
  }

  private void decrypt(MessageOnion onion) {
    EncryptedData encryptedData = onion.unwrapMsg(EncryptedData.class);
    byte[] decrypted = encryptService.ecDecrypt(onion.getSigner().key, encryptedData.data);

    onion.loadWrappedData(decrypted);
  }
}
