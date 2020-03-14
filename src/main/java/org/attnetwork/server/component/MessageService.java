package org.attnetwork.server.component;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.proto.msg.wrapper.WrapType;
import org.attnetwork.proto.sl.AbstractSeqLanObject;

public interface MessageService {
  void process(InputStream is, OutputStream os);

  MessageOnion wrap(String type, AbstractSeqLanObject msg, AsmPublicKeyChain signer, List<WrapType> wrapTypes);
}
