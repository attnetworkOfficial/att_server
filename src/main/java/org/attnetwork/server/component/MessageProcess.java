package org.attnetwork.server.component;

import java.util.ArrayList;
import java.util.List;
import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.exception.AException;
import org.attnetwork.proto.msg.wrapper.TypedMsg;
import org.attnetwork.proto.msg.wrapper.WrapType;
import org.attnetwork.proto.msg.wrapper.WrappedMsg;
import org.attnetwork.proto.sl.AbstractSeqLanObject;

public class MessageProcess {
  public String type;
  public WrappedMsg wrappedMsg;
  public List<AsmPublicKeyChain> signers;
  public byte[] sessionId;
  public TypedMsg typedMsg;
  private boolean finalized;

  public void loadWrappedData(byte[] data) {
    wrappedMsg = AbstractSeqLanObject.read(data, WrappedMsg.class);
  }

  public <T extends AbstractSeqLanObject> T unwrapMsg(Class<T> type) {
    return AbstractSeqLanObject.read(wrappedMsg.data, type);
  }

  public boolean needUnwrap() {
    return wrappedMsg.wrapType != null;
  }

  public WrapType getWrapType() {
    return wrappedMsg.wrapType == null ? null : WrapType.getByCode(wrappedMsg.wrapType);
  }

  public byte[] getWrapData() {
    return wrappedMsg.data;
  }

  public void addSigner(AsmPublicKeyChain signer) {
    if (signers == null) {
      signers = new ArrayList<>();
    }
    signers.add(signer);
  }

  public MessageProcess doFinal() {
    if (finalized) {
      throw new AException("already finalized");
    }
    finalized = true;
    typedMsg = unwrapMsg(TypedMsg.class);
    return this;
  }

  public String getMsgType() {
    return typedMsg == null || typedMsg.type == null ? "" : typedMsg.type;
  }
}
