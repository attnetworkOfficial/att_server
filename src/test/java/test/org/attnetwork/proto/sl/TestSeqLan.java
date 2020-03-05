package test.org.attnetwork.proto.sl;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import org.attnetwork.proto.sl.AbstractMsg;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;
import test.org.attnewtork.proto.msg.ExampleUserContactMsg;
import test.org.attnewtork.proto.msg.ExampleUserMsg;

class TestSeqLan {
  @Test
  void testExampleMsg() {
    ExampleUserMsg user = new ExampleUserMsg();
    user.id = 11099822739479112L;
    user.firstName = "John";
    user.lastName = "Smith";
    user.score = new BigDecimal("128.32");
    user.phone = "400-222-5555";

    ExampleUserContactMsg contact1 = new ExampleUserContactMsg();
    contact1.id = 39817873987985719L;
    contact1.remark = "boss";
    ExampleUserContactMsg contact2 = new ExampleUserContactMsg();
    contact2.id = 45405687374639045L;
    user.contacts = new ExampleUserContactMsg[] {contact1, contact2};


    ByteArrayOutputStream os = new ByteArrayOutputStream();
    user.write(os);
    byte[] rawA = os.toByteArray();
    System.out.println("raw: " + ByteUtils.toHexString(rawA));

    ExampleUserMsg msgB = AbstractMsg.read(rawA, ExampleUserMsg.class);
    os = new ByteArrayOutputStream();
    msgB.write(os);
    byte[] rawB = os.toByteArray();

    Assert.isTrue(ByteUtils.equals(rawA, rawB), "recovery raw message error");
  }
}
