package test.org.attnetwork.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.attnetwork.crypto.ECCrypto;
import org.attnetwork.crypto.asymmetric.AsmKeyPair;
import org.attnetwork.crypto.asymmetric.AsmPublicKey;
import org.attnetwork.proto.attn.AtTnProto;
import org.attnetwork.proto.msg.SessionStartMsg;
import org.attnetwork.proto.msg.SessionStartMsgResp;
import org.attnetwork.proto.msg.wrapper.TypedMsg;
import org.attnetwork.proto.msg.wrapper.WrapType;
import org.attnetwork.server.Application;
import org.attnetwork.server.component.MessageOnion;
import org.attnetwork.server.component.MessageService;
import org.attnetwork.server.component.MessageType;
import org.attnetwork.server.component.l2.EncryptServiceL2;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

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
  void testSession() {
    // simulate the client, trying to start session
    SessionStartMsg msg = new SessionStartMsg();
    msg.attnVersion = AtTnProto.DEBUG.VERSION;
    msg.random = new byte[128];
    encryptService.setup(userKeyPair);

    MessageOnion onion = MessageOnion
        .sow(MessageType.START_SESSION, msg)
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
    onion = MessageOnion.sow(is);
    messageService.unwrap(onion);
    SessionStartMsgResp sessionStartMsgResp = onion.readTypedMsg(SessionStartMsgResp.class);

    // client send ATTN protocol encrypted msg
    onion = MessageOnion
        .sow(MessageType.__DEBUG__, null)
        .setWrapTypes(WrapType.L_ATTN_PROTO)
        .setSessionId(sessionStartMsgResp.sessionId);
    messageService.wrap(onion);

    // server receive ping message
    is = new ByteArrayInputStream(onion.getWrappedMsg().getRaw());
    os = new ByteArrayOutputStream();
    messageService.process(is, os);

    // receive encrypted msg
    is = new ByteArrayInputStream(os.toByteArray());
    onion = MessageOnion.sow(is);
    messageService.unwrap(onion);

    TypedMsg typedMsg = onion.getTypedMsg();
    Assert.isTrue(MessageType.__DEBUG__.equals(typedMsg.type), "debug error");

    // client send ATTN protocol encrypted msg
    onion = MessageOnion
        .sow(MessageType.PING, null)
        .setWrapTypes(WrapType.L_ATTN_PROTO)
        .setSessionId(sessionStartMsgResp.sessionId);
    messageService.wrap(onion);

    // server receive ping message
    is = new ByteArrayInputStream(onion.getWrappedMsg().getRaw());
    os = new ByteArrayOutputStream();
    messageService.process(is, os);

    // receive ping msg
    Assert.isTrue(ByteUtils.equals(os.toByteArray(), new byte[]{0}), "debug error");
  }
}
