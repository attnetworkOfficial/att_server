package test.org.attnetwork.proto;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import test.org.attnetwork.proto.pb.msg.ProtocolBufferTest;
import test.org.attnetwork.proto.sl.msg.ExampleUserContactMsg;
import test.org.attnetwork.proto.sl.msg.ExampleUserMsg;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Random;

@Disabled
class CompareProto {

  @Test
  void testSerializeSpeed() throws IOException {
    int round = 100000;
    long t = System.currentTimeMillis();
    for (int i = 0; i < round; i++) {
      ProtocolBufferTest.ExampleUserMsg.Builder u1 = ProtocolBufferTest.ExampleUserMsg.newBuilder();
      u1.setId(11099822739479112L)
          .setFirstName("John")
          .setLastName("Smith")
          .setScore(128.32F)
          .setPhone("400-222-5555")
          .addContacts(ProtocolBufferTest.ExampleUserContactMsg.newBuilder().setId(39817873987985719L).setRemark("boss"))
          .addContacts(ProtocolBufferTest.ExampleUserContactMsg.newBuilder().setId(45405687374639045L));

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      u1.build().writeTo(os);
    }

    System.out.println(System.currentTimeMillis() - t);

    t = System.currentTimeMillis();


    for (int i = 0; i < round; i++) {

      ExampleUserMsg u2 = new ExampleUserMsg();
      u2.id = 11099822739479112L;
      u2.firstName = "John";
      u2.lastName = "Smith";
      u2.score = new BigDecimal("128.32");
      u2.phone = "400-222-5555";

      ExampleUserContactMsg contact1 = new ExampleUserContactMsg();
      contact1.id = 39817873987985719L;
      contact1.remark = "boss";
      ExampleUserContactMsg contact2 = new ExampleUserContactMsg();
      contact2.id = 45405687374639045L;
      u2.contacts = new ExampleUserContactMsg[]{contact1, contact2};

      ByteArrayOutputStream os = new ByteArrayOutputStream();
      u2.write(os);
    }
    System.out.println(System.currentTimeMillis() - t);
  }


  @Test
  void testDataSize() throws IOException {
    int round = 10000;
    Random random = new Random();

    ProtocolBufferTest.ExampleUserMsg.Builder u1 = ProtocolBufferTest.ExampleUserMsg.newBuilder();
    u1.setId(11099822739479112L)
        .setFirstName("John")
        .setLastName("Smith")
        .setScore(128.32F)
        .setPhone("400-222-5555");
    for (int i = 0; i < round; i++) {
      if (random.nextBoolean())
        u1.addContacts(ProtocolBufferTest.ExampleUserContactMsg.newBuilder()
            .setId(random.nextLong())
            .setRemark(String.valueOf(random.nextLong()))
        );
      else {
        u1.addContacts(ProtocolBufferTest.ExampleUserContactMsg.newBuilder());
      }
    }

    ByteArrayOutputStream os = new ByteArrayOutputStream();
    ProtocolBufferTest.ExampleUserMsg build = u1.build();
    build.writeTo(os);

    System.out.println(os.toByteArray().length);

    // 2

    ExampleUserMsg u2 = new ExampleUserMsg();
    u2.id = 11099822739479112L;
    u2.firstName = "John";
    u2.lastName = "Smith";
    u2.score = new BigDecimal("128.32");
    u2.phone = "400-222-5555";
    u2.contacts = new ExampleUserContactMsg[round];

    for (int i = 0; i < round; i++) {

      ExampleUserContactMsg contact1 = new ExampleUserContactMsg();
      if (random.nextBoolean()) {
        contact1.id = random.nextLong();
        contact1.remark = String.valueOf(random.nextLong());
      }
      u2.contacts[i] = contact1;
    }


    os = new ByteArrayOutputStream();
    u2.write(os);

    System.out.println(os.toByteArray().length);
  }
}
