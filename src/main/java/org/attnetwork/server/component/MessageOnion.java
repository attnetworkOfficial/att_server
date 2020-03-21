package org.attnetwork.server.component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.exception.AException;
import org.attnetwork.proto.msg.wrapper.TypedMsg;
import org.attnetwork.proto.msg.wrapper.WrapType;
import org.attnetwork.proto.msg.wrapper.WrappedMsg;
import org.attnetwork.proto.sl.AbstractSeqLanObject;

public class MessageOnion {
  private TypedMsg typedMsg;
  private List<WrapType> wrapTypes;
  private WrappedMsg wrappedMsg;
  private AsmPublicKeyChain signer;
  private Integer sessionId;

  public static MessageOnion sow(InputStream is) {
    MessageOnion onion = new MessageOnion();
    onion.wrappedMsg = AbstractSeqLanObject.read(is, WrappedMsg.class);
    return onion;
  }

  public static MessageOnion sow(String type, AbstractSeqLanObject msg) {
    return new MessageOnion().revive(type, msg);
  }

  public MessageOnion revive(AbstractSeqLanObject msg) {
    return revive(null, msg);
  }

  public MessageOnion revive(String type, AbstractSeqLanObject msg) {
    core(type, msg);
    wrap(null, typedMsg);
    return this;
  }

  public void harvest(OutputStream os) throws IOException {
    wrappedMsg.write(os);
  }

  public AbstractSeqLanObject getProcessingMsg() {
    return wrappedMsg == null ? typedMsg : wrappedMsg;
  }

  public WrappedMsg getWrappedMsg() {
    return wrappedMsg;
  }

  public void loadWrappedData(byte[] data) {
    wrappedMsg = AbstractSeqLanObject.read(data, WrappedMsg.class);
  }

  public MessageOnion core(String type, AbstractSeqLanObject msg) {
    typedMsg = TypedMsg.wrap(type, msg);
    return this;
  }

  public MessageOnion wrap(WrapType wrapType, AbstractSeqLanObject msg) {
    wrappedMsg = WrappedMsg.wrap(wrapType, msg);
    return this;
  }

  public <T extends AbstractSeqLanObject> T unwrapMsg(Class<T> type) {
    if (wrapTypes == null) {
      wrapTypes = new ArrayList<>();
    }
    wrapTypes.add(getWrapType());
    return AbstractSeqLanObject.read(wrappedMsg.data, type);
  }

  public void checkWrapTypes(List<WrapType> list) {
    if (!list.equals(wrapTypes)) {
      throw new AException("unexpected wrap types: " + wrapTypes + ", requiring types: " + list);
    }
  }

  public MessageOnion setWrapTypes(List<WrapType> wrapTypes) {
    this.wrapTypes = wrapTypes;
    return this;
  }

  public List<WrapType> getWrapTypes() {
    return wrapTypes;
  }

  public WrapType getWrapType() {
    return wrappedMsg == null || wrappedMsg.wrapType == null ? null : WrapType.getByCode(wrappedMsg.wrapType);
  }

  public String getMsgType() {
    return typedMsg == null || typedMsg.type == null ? "null" : typedMsg.type;
  }

  public void readTypedMsgFromWrappedMsg() {
    typedMsg = AbstractSeqLanObject.read(wrappedMsg.data, TypedMsg.class);
  }

  public <T extends AbstractSeqLanObject> T readTypedMsg(Class<T> type) {
    return AbstractSeqLanObject.read(typedMsg.data, type);
  }

  public AsmPublicKeyChain getSigner() {
    return signer;
  }

  public MessageOnion setSigner(AsmPublicKeyChain signer) {
    this.signer = signer;
    return this;
  }

  public Integer getSessionId() {
    return sessionId;
  }

  public MessageOnion setSessionId(Integer sessionId) {
    this.sessionId = sessionId;
    return this;
  }

  public TypedMsg getTypedMsg() {
    return typedMsg;
  }
}
