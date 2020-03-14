package org.attnetwork.server.component.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.attnetwork.crypto.EncryptedData;
import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.exception.AException;
import org.attnetwork.proto.msg.ErrorMsg;
import org.attnetwork.proto.msg.SessionStartMsg;
import org.attnetwork.proto.msg.SessionStartMsgResp;
import org.attnetwork.proto.msg.wrapper.AtTnMsg;
import org.attnetwork.proto.msg.wrapper.SignedMsg;
import org.attnetwork.proto.msg.wrapper.TypedMsg;
import org.attnetwork.proto.msg.wrapper.WrapType;
import org.attnetwork.proto.msg.wrapper.WrappedMsg;
import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.attnetwork.server.component.MessageOnion;
import org.attnetwork.server.component.MessageService;
import org.attnetwork.server.component.l2.EncryptServiceL2;
import org.attnetwork.server.component.l2.SessionServiceL2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageServiceImpl implements MessageService {

  private final SessionServiceL2 sessionService;
  private final EncryptServiceL2 encryptService;
  private final Logger log = LoggerFactory.getLogger(MessageService.class);

  @Autowired
  public MessageServiceImpl(SessionServiceL2 sessionService, EncryptServiceL2 encryptService) {
    this.sessionService = sessionService;
    this.encryptService = encryptService;
  }

  @Override
  public void process(InputStream is, OutputStream os) {
    try {
      MessageOnion process = MessageOnion.read(is);
      unwrap(process);
      String msgType = process.getMsgType();
      switch (msgType) {
        case TypedMsg.START_SESSION:
          process.checkWrapTypes(WrapType.L_SIGN_ENCRYPT);
          SessionStartMsgResp resp = sessionService.startSession(
              process.readTypedMsg(SessionStartMsg.class), process.getSigner());
          process = wrap(null, resp, process.getSigner(), WrapType.L_ENCRYPT_SIGN);
          break;
        default:
          throw new AException("unsupported message type: " + msgType);
      }
      process.getWrappedMsg().write(os);
    } catch (Exception e) {
      exceptionHandle(e, os);
    }
  }

  private void exceptionHandle(Exception e, OutputStream os) {
    try {
      ErrorMsg errorMsg = new ErrorMsg();
      errorMsg.code = e instanceof AException ? ((AException) e).getCode() : "400";
      errorMsg.msg = e.getMessage();
      TypedMsg typedMsg = MessageOnion.wrapTypedMsg("resp.error", errorMsg);
      WrappedMsg.wrap(null, typedMsg).write(os);
    } catch (IOException ioE) {
      log.error("write outputStream error!", ioE.getMessage());
    }
  }

  @Override
  public MessageOnion wrap(String type, AbstractSeqLanObject msg, AsmPublicKeyChain signer, List<WrapType> wrapTypes) {
    MessageOnion process = MessageOnion.write(type, msg);
    process.setSigner(signer);
    for (WrapType wrapType : wrapTypes) {
      switch (wrapType) {
        case ATTN_PROTO:
          break;
        case SIGN:
          sign(process);
          break;
        case ENCRYPT:
          encrypt(process);
          break;
      }
    }
    return process;
  }

  private void unwrap(MessageOnion process) {
    WrapType wrapType;
    while ((wrapType = process.getWrapType()) != null) {
      switch (wrapType) {
        case ATTN_PROTO:
          unwrapAtTnProto(process);
          break;
        case SIGN:
          checkSign(process);
          break;
        case ENCRYPT:
          decrypt(process);
          break;
      }
    }
    process.readTypedMsgFromWrappedMsg();
  }

  private void unwrapAtTnProto(MessageOnion process) {
    AtTnMsg atTnMsg = process.unwrapMsg(AtTnMsg.class);
    // TODO decode ATTN_Proto data
    byte[] decrypted = atTnMsg.data;

//    process.setSessionId(atTnMsg.sessionId);
    process.loadWrappedData(decrypted);
  }

  // ----------------------------------------------------------------------------------------
  private void sign(MessageOnion process) {
    byte[] data = process.getProcessingMsgData();
    SignedMsg signedMsg = new SignedMsg();
    signedMsg.data = data;
    signedMsg.sign = encryptService.ecSign(data);
    signedMsg.publicKeyChain = encryptService.getPublicKeyChain();
    process.wrapMsg(WrapType.SIGN, signedMsg);
  }

  private void checkSign(MessageOnion process) {
    SignedMsg signedMsg = process.unwrapMsg(SignedMsg.class);
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
    process.addSigner(signedMsg.publicKeyChain);
    process.loadWrappedData(signedMsg.data);
  }

  // ----------------------------------------------------------------------------------------
  private void decrypt(MessageOnion process) {
    EncryptedData encryptedData = process.unwrapMsg(EncryptedData.class);
    byte[] decrypted = encryptService.ecDecrypt(process.getSigner().key, encryptedData.data);
    process.loadWrappedData(decrypted);
  }

  private void encrypt(MessageOnion process) {
    byte[] data = process.getProcessingMsgData();
    EncryptedData encryptedData = encryptService.ecEncrypt(process.getSigner().key, data);
    process.wrapMsg(WrapType.ENCRYPT, encryptedData);
  }
}
