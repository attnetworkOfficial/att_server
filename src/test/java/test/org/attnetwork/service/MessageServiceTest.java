package test.org.attnetwork.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.attnetwork.crypto.asymmetric.AsmKeyPair;
import org.attnetwork.proto.msg.SessionStartMsg;
import org.attnetwork.proto.msg.SessionStartMsgResp;
import org.attnetwork.proto.msg.wrapper.TypedMsg;
import org.attnetwork.proto.msg.wrapper.WrapType;
import org.attnetwork.server.Application;
import org.attnetwork.server.component.MessageOnion;
import org.attnetwork.server.component.MessageService;
import org.attnetwork.server.component.l2.EncryptServiceL2;
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

  @BeforeEach
  void before() {
    String serverKeyPairString = "gywgYHdWq7L6dCVfzXYWyvTwLRQkSe/ud4nDwwXVmxi54v+DCUYMRUMtc2VjcDI1NmsxBgFw483DkwYBcOj0H5MGAXDjzcOTAAAhA0YI5ryAx9RUmsHSxX31QOUzlEQy3GUWFO+41cN3LeWsWA9TSEEyNTZ3aXRoRUNEU0FHMEUCIQCrqbu9IbB1VDjuFAdzLnzoz/a2hOVJhEOQIljC2QR9/wIgL1NTAtmdtnQJpSC7/oxU7ADKh9Wehnt6u/7FKXzRPWWBZ0YMRUMtc2VjcDI1NmsxBgFw3N/s/wYBcQDscP8GAXDc3+z/AAAhAp8MYTKPL3chsJRhIQ3PO+j2aGJ0y9Z4UScAZK04YnXnWA9TSEEyNTZ3aXRoRUNEU0FHMEUCIBHiZu2wVL3BkyxBEeMOz6fUrIvPRWRDc/dQD78qJbnpAiEAre4BdcbvrpcGIztnOcExymWS/o/Lt27UuKMOtbNf46dGQwxFQy1zZWNwMjU2azEAAAYBcNzfg80ACVRFU1QtUk9PVCECRjkJZ1gsZW1kiCus3xuqD54ot1E10ed8Xo4jWBIC+bIAAA==";
    encryptService.setup(AsmKeyPair.readBase64String(serverKeyPairString, AsmKeyPair.class));
    String userKeyPairString = "gywgYHdWq7L6dCVfzXYWyvTwLRQkSe/ud4nDwwXVmxi54v+DCUYMRUMtc2VjcDI1NmsxBgFw483DkwYBcOj0H5MGAXDjzcOTAAAhA0YI5ryAx9RUmsHSxX31QOUzlEQy3GUWFO+41cN3LeWsWA9TSEEyNTZ3aXRoRUNEU0FHMEUCIQCrqbu9IbB1VDjuFAdzLnzoz/a2hOVJhEOQIljC2QR9/wIgL1NTAtmdtnQJpSC7/oxU7ADKh9Wehnt6u/7FKXzRPWWBZ0YMRUMtc2VjcDI1NmsxBgFw3N/s/wYBcQDscP8GAXDc3+z/AAAhAp8MYTKPL3chsJRhIQ3PO+j2aGJ0y9Z4UScAZK04YnXnWA9TSEEyNTZ3aXRoRUNEU0FHMEUCIBHiZu2wVL3BkyxBEeMOz6fUrIvPRWRDc/dQD78qJbnpAiEAre4BdcbvrpcGIztnOcExymWS/o/Lt27UuKMOtbNf46dGQwxFQy1zZWNwMjU2azEAAAYBcNzfg80ACVRFU1QtUk9PVCECRjkJZ1gsZW1kiCus3xuqD54ot1E10ed8Xo4jWBIC+bIAAA==";
    userKeyPair = AsmKeyPair.readBase64String(userKeyPairString, AsmKeyPair.class);
  }

  @Test
  void testSessionStart() {
    SessionStartMsg msg = new SessionStartMsg();
    msg.algorithm = "default";
    msg.random = new byte[128];
    MessageOnion onion = messageService.wrap("start_session", msg, userKeyPair.publicKeyChain, WrapType.L_ENCRYPT_SIGN);
    ByteArrayInputStream is = new ByteArrayInputStream(onion.getWrappedMsg().getRaw());
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    messageService.process(is, os);

    is = new ByteArrayInputStream(os.toByteArray());
    MessageOnion process = MessageOnion.read(is);
    messageService.unwrap(process);
    SessionStartMsgResp sessionStartMsgResp = process.readTypedMsg(SessionStartMsgResp.class);
    System.out.println(sessionStartMsgResp.sessionId);
  }
}
