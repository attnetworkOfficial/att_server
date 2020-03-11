package test.org.attnetwork.http;

import org.attnetwork.proto.msg.SessionStartMsg;
import org.attnetwork.server.Application;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Application.class)
public class HttpTest {

  @Test
  void testSessionStart() {
    SessionStartMsg msg = new SessionStartMsg();
//    msg.wrap();
  }
}
