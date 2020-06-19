package test.org.attnetwork.proto.pb;

import org.junit.jupiter.api.Test;
import test.org.attnetwork.proto.pb.msg.ProtocolBufferTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class TestProtoBuffer {

  @Test
  void testExampleMsg() throws IOException {
    ProtocolBufferTest.ExampleUserMsg.Builder user = ProtocolBufferTest.ExampleUserMsg.newBuilder();
    user.setId(11099822739479112L)
        .setFirstName("John")
        .setLastName("Smith")
        .setScore(128.32F)
        .setPhone("400-222-5555")
        .addContacts(ProtocolBufferTest.ExampleUserContactMsg.newBuilder().setId(39817873987985719L).setRemark("boss"))
        .addContacts(ProtocolBufferTest.ExampleUserContactMsg.newBuilder().setId(45405687374639045L));

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    user.build().writeTo(os);
  }
}