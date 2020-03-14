package test.org.attnetwork.service;

import java.io.ByteArrayInputStream;
import org.attnetwork.proto.msg.SessionStartMsg;
import org.attnetwork.proto.msg.wrapper.TypedMsg;
import org.attnetwork.proto.msg.wrapper.WrapType;
import org.attnetwork.server.Application;
import org.attnetwork.server.component.MessageOnion;
import org.attnetwork.server.component.MessageService;
import org.attnetwork.server.component.l2.EncryptServiceL2;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Application.class)
class MessageServiceTest {

  @Autowired
  private MessageService messageService;
  @Autowired
  private EncryptServiceL2 encryptService;

  @BeforeAll
  static void beforeAll() {
    String keyChain = "IB+GPnUZxi886DGDar5oiDrXzcH07/LbnqR1FF+8FzDFgmo+DEVDLXNlY3AyNTZrMQYBcNlSLU8GAXDZUjE3ACECtnVCn0ss9mxTcFRaeebwz4FqVCzd7tODuJ4XjPqgzdFZD1NIQTI1NndpdGhFQ0RTQUgwRgIhALyCirXX1c5gfTfvGW5ujTOIIPIXlg6jMyvNosrVTXEMAiEAijQ6NnzrgMHDbh0w3hht99T2n3OA3zrjZ1AAPCE6d2aBTz4MRUMtc2VjcDI1NmsxBgFw2VItTwYBcNlSVF8AIQL6CSno0g2rjLqkRriSCfqVg2WnBVFzIDdsCJAMv2YN3lkPU0hBMjU2d2l0aEVDRFNBSDBGAiEAvKKv3cdEJrAjHBgO8vS/YcC14D9rnodi1CJPaWrW9UYCIQDf6sFE3STZ/9wCPBGng+lM3ObxujfHh7YPlAy+PtdPYjUyDEVDLXNlY3AyNTZrMQAAACEDlexqPo//QhFjijT6pmTMed/KazOG0ILRvAkq+qKEsZ4AAA==";
//    encryptService.setup();
  }

  @Test
  void testSessionStart() {
    SessionStartMsg msg = new SessionStartMsg();
    msg.algorithm = "default";
    msg.random = new byte[128];
    MessageOnion onion = messageService.wrap(TypedMsg.START_SESSION, msg, null, WrapType.L_ENCRYPT_SIGN);

    ByteArrayInputStream is = new ByteArrayInputStream(onion.getWrappedMsg().getRaw());
//    msg.wrap();
  }
}
