package test.org.attnetwork.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.attnetwork.crypto.ECCrypto;
import org.attnetwork.crypto.asymmetric.AsmKeyPair;
import org.attnetwork.crypto.asymmetric.AsmPublicKey;
import org.attnetwork.proto.attn.AttnProto;
import org.attnetwork.proto.msg.SessionStartMsg;
import org.attnetwork.proto.msg.SessionStartMsgResp;
import org.attnetwork.proto.msg.wrapper.WrapType;
import org.attnetwork.server.Application;
import org.attnetwork.server.component.MessageOnion;
import org.attnetwork.server.component.MessageService;
import org.attnetwork.server.component.l2.EncryptServiceL2;
import org.attnetwork.utils.HashUtil;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Application.class)
class MessageServiceTest {

  @Autowired
  private MessageService messageService;
  @Autowired
  private EncryptServiceL2 encryptService;

  private AsmKeyPair userKeyPair;
  private AsmKeyPair serverKeyPair;

  @BeforeEach
  void before() {
    ECCrypto ecc = ECCrypto.instance();
    long now = System.currentTimeMillis();
    userKeyPair = ecc.generateSubKey(ecc.generateRootKey(), AsmPublicKey.preGen().end(now + 1000));
    serverKeyPair = ecc.generateSubKey(ecc.generateRootKey(), AsmPublicKey.preGen().end(now + 1000));
  }

  @Test
  void testSessionStart() {
    // simulate the client, trying to start session
    SessionStartMsg msg = new SessionStartMsg();
    msg.attnVersion = AttnProto.DEBUG.VERSION;
    msg.random = new byte[128];
    encryptService.setup(userKeyPair);

    MessageOnion onion = MessageOnion
        .sow("start_session", msg)
        .setSigner(serverKeyPair.publicKeyChain)
        .setWrapTypes(WrapType.L_ENCRYPT_SIGN);

    messageService.wrap(onion);

    // server receive the message
    encryptService.setup(serverKeyPair);
    ByteArrayInputStream is = new ByteArrayInputStream(onion.getWrappedMsg().getRaw());
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    messageService.process(is, os);

    // client get the response
    encryptService.setup(userKeyPair);
    is = new ByteArrayInputStream(os.toByteArray());
    MessageOnion process = MessageOnion.irrigation(is);
    messageService.unwrap(process);
    SessionStartMsgResp sessionStartMsgResp = process.readTypedMsg(SessionStartMsgResp.class);
    System.out.println(sessionStartMsgResp.sessionId);
    //
    byte[] sharedSecret = HashUtil.sha512(msg.random, sessionStartMsgResp.random);
    System.out.println(ByteUtils.toHexString(sharedSecret));
  }
}
