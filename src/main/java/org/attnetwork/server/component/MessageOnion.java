package org.attnetwork.server.component;

import java.io.InputStream;
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

  public static MessageOnion read(InputStream is) {
    MessageOnion process = new MessageOnion();
    process.wrappedMsg = AbstractSeqLanObject.read(is, WrappedMsg.class);
    return process;
  }

  public static MessageOnion write(String type, AbstractSeqLanObject msg) {
    MessageOnion process = new MessageOnion();
    process.wrappedMsg = WrappedMsg.wrap(null, wrapTypedMsg(type, msg));
    return process;
  }

  public static TypedMsg wrapTypedMsg(String type, AbstractSeqLanObject msg) {
    TypedMsg typedResp = new TypedMsg();
    typedResp.type = type;
    typedResp.data = msg.getRaw();
    return typedResp;
  }

  public byte[] getProcessingMsgData() {
    return wrappedMsg == null ? typedMsg.data : wrappedMsg.data;
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

  public void wrapMsg(WrapType wrapType, AbstractSeqLanObject msg) {
    wrappedMsg = WrappedMsg.wrap(wrapType, msg);
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

  public WrapType getWrapType() {
    return wrappedMsg == null || wrappedMsg.wrapType == null ? null : WrapType.getByCode(wrappedMsg.wrapType);
  }

  public void addSigner(AsmPublicKeyChain signer) {
    this.signer = signer;
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

  public void setSigner(AsmPublicKeyChain signer) {
    this.signer = signer;
  }
}
