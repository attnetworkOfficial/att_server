package test.org.attnetwork.proto.sl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;
import test.org.attnewtork.proto.msg.ExampleUserContactMsg;
import test.org.attnewtork.proto.msg.ExampleUserMsg;

class TestSeqLan {

  @Test
  void testExampleMsg() throws IOException {
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
    user.contacts = new ExampleUserContactMsg[]{contact1, contact2};

    String rawHex = "4007276f3adf74da48044a6f686e05536d69746804023220020c3430302d3232322d353535351a0e08008d76253ac3e13704626f73730a0800a1503b6abf6fc500";

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    user.write(os);
    byte[] rawA = os.toByteArray();
    Assert.isTrue(ByteUtils.equals(rawA, ByteUtils.fromHexString(rawHex)),
                  "rawA should equals rawHex\nA: " + ByteUtils.toHexString(rawA) + "\nR: " + rawHex);

    ExampleUserMsg msgB = AbstractSeqLanObject.read(new ByteArrayInputStream(rawA), ExampleUserMsg.class);
    os = new ByteArrayOutputStream();
    msgB.write(os);
    byte[] rawB = os.toByteArray();

    Assert.isTrue(ByteUtils.equals(rawB, ByteUtils.fromHexString(rawHex)),
                  "rawB should equals rawHex\nB: " + ByteUtils.toHexString(rawB) + "\nR: " + rawHex);
  }
}

// 4107276f3adf74da48044a6f686e05536d697468 050102023220 0c3430302d3232322d353535351a0e08008d76253ac3e13704626f73730a0800a1503b6abf6fc500
// 4007276f3adf74da48044a6f686e05536d697468 0402322002   0c3430302d3232322d353535351a0e08008d76253ac3e13704626f73730a0800a1503b6abf6fc500
// 4107276f3adf74da48044a6f686e05536d697468 05023220020c 0c3430302d3232322d353535351a0e08008d76253ac3e13704626f73730a0800a1503b6abf6fc500
