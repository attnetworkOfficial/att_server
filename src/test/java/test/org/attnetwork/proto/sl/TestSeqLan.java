package test.org.attnetwork.proto.sl;

import java.io.ByteArrayOutputStream;
import org.attnetwork.proto.sl.AbstractMsg;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;
import test.org.attnewtork.proto.msg.ExampleMsg;

class TestSeqLan {
  @Test
  void testExampleMsg() {
    ExampleMsg msgA = new ExampleMsg();
    msgA.id = 1;
    msgA.msg = "hello";

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    msgA.write(os);
    byte[] rawA = os.toByteArray();

    ExampleMsg msgB = AbstractMsg.read(rawA, ExampleMsg.class);
    os = new ByteArrayOutputStream();
    msgB.write(os);
    byte[] rawB = os.toByteArray();

    Assert.isTrue(ByteUtils.equals(rawA, rawB), "recovery raw message error");
    System.out.println("raw: " + ByteUtils.toHexString(rawB));
  }
}
