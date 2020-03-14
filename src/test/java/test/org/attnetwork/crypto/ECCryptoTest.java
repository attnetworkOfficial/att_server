package test.org.attnetwork.crypto;

import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import org.attnetwork.crypto.ECCrypto;
import org.attnetwork.crypto.ECKeyPair;
import org.attnetwork.crypto.asymmetric.AsmKeyPair;
import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.utils.HashUtil;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.spec.IEKeySpec;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

class ECCryptoTest {
  private Logger log = LoggerFactory.getLogger(getClass());
  private ECCrypto ecc = ECCrypto.instance();

  @Test
  void testGeneratePrivateKey() {
    ECKeyPair oldK = ecc.generateKeyPair();
    BigInteger oldD = oldK.getPrivateKey().getD();
    byte[] oldPub = oldK.getPublicKey().getQ().getEncoded(true);

    ECKeyPair newK = ecc.restoreKeyPair(oldD.toString(16));
    BigInteger newD = newK.getPrivateKey().getD();
    byte[] newPub = newK.getPublicKey().getQ().getEncoded(true);

    Assert.isTrue(oldD.equals(newD), "private key check fail");
    Assert.isTrue(ByteUtils.equals(oldPub, newPub), "public key check fail");
  }

  @Test
  void testSign() throws InterruptedException {
    int round = 3, thread = 1;
    CountDownLatch countDownLatch = new CountDownLatch(round * thread);
    for (int i = 0; i < thread; i++) {
      new Thread(() -> testSignVerify(round, countDownLatch)).start();
    }
    countDownLatch.await();
  }

  private void testSignVerify(int round, CountDownLatch countDownLatch) {
    ECKeyPair pair = ecc.generateKeyPair();
    BCECPrivateKey privateKey = pair.getPrivateKey();
    BCECPublicKey publicKey = pair.getPublicKey();

    for (int i = 0; i < round; i++) {
      byte[] data = HashUtil.sha256(("asdfafds").getBytes());
      String info = "prv: " + privateKey.getD().toString(16) +
                    "\ndata: " + ByteUtils.toHexString(data);

      BigInteger[] sign = ecc.sign(privateKey.getD(), data);
      boolean verify = ecc.verify(publicKey.getQ(), sign, data);
      Assert.isTrue(verify, "verify ecc sign fail\n" + info);


      byte[] sign2 = ecc.sign(privateKey, data).data;
      log.debug("r:{} s:{}\ns2:{}", sign[0].toString(16), sign[1].toString(16), ByteUtils.toHexString(sign2));

      int v = ecc.generateSignV(publicKey.getQ(), sign, data);
      verify = ecc.verify(v, sign, data);
      Assert.isTrue(verify, "verify ecc from recovery public key fail\n" + info);
      countDownLatch.countDown();
    }
    log.trace("test sign verify finish");
  }

  @Test
  void testSignV2() throws InterruptedException {
    int round = 100;
    CountDownLatch countDownLatch = new CountDownLatch(round * 3);
    new Thread(() -> testSignVerifyV2(round, countDownLatch)).start();
    new Thread(() -> testSignVerifyV2(round, countDownLatch)).start();
    testSignVerifyV2(round, countDownLatch);

    countDownLatch.await();
  }

  private void testSignVerifyV2(int round, CountDownLatch countDownLatch) {
    for (int i = 0; i < round; i++) {
      ECKeyPair pair = ecc.generateKeyPair();
      BCECPrivateKey privateKey = pair.getPrivateKey();
      BCECPublicKey publicKey = pair.getPublicKey();
      byte[] data = HashUtil.sha256(("asdfafds" + i).getBytes());
      String info = "prv: " + privateKey.getD().toString(16) +
                    "\ndata: " + ByteUtils.toHexString(data);
      byte[] sign = ecc.sign(privateKey, data).data;
      boolean verify = ecc.verify(publicKey, sign, data);
      Assert.isTrue(verify, "verify ecc sign fail\n" + info);

      countDownLatch.countDown();
    }
    log.trace("test sign verify finish");
  }


  @Test
  void testCrypt() {
    ECKeyPair alice = ecc.generateKeyPair();
    ECKeyPair bob = ecc.generateKeyPair();

    byte[] origin = new byte[1000];
    byte[] encrypt = ecc.encrypt(new IEKeySpec(alice.getPrivateKey(), bob.getPublicKey()), origin).data;
    System.out.println(ByteUtils.toHexString(encrypt));
    byte[] decrypt = ecc.decrypt(new IEKeySpec(bob.getPrivateKey(), alice.getPublicKey()), encrypt);

    Assert.isTrue(ByteUtils.equals(origin, decrypt), "fail");

    encrypt = ecc.encrypt(alice.getPublicKey(), origin).data;
    System.out.println(ByteUtils.toHexString(encrypt));
    decrypt = ecc.decrypt(alice.getPrivateKey(), encrypt);

    Assert.isTrue(ByteUtils.equals(origin, decrypt), "fail");
  }

  @Test
  void testKeyChain() {
    AsmKeyPair rootKeyPair = ecc.generateRootKey();
    byte[] rootPublicKey = rootKeyPair.publicKeyChain.key.data;
    Long now = System.currentTimeMillis();

    AsmKeyPair l2KeyPair = ecc.generateSubKey(rootKeyPair, now, now + 10_000L);
    AsmPublicKeyChain.Validation validation = l2KeyPair.publicKeyChain.isValid(rootPublicKey, ecc);
    Assert.isTrue(validation.isValid, "l2 verify fail: " + validation);

    AsmKeyPair l3KeyPair = ecc.generateSubKey(l2KeyPair, now, now + 1_000L);
    validation = l3KeyPair.publicKeyChain.isValid(rootPublicKey, ecc);
    Assert.isTrue(validation.isValid, "l3 verify fail: " + validation);

    AsmKeyPair l4KeyPair = ecc.generateSubKey(l3KeyPair, now, now - 1L);
    validation = l4KeyPair.publicKeyChain.isValid(rootPublicKey, ecc);
    Assert.isTrue(!validation.isValid, "l4 expire fail");

    // recreate l4 key pair
    l4KeyPair = ecc.generateSubKey(l3KeyPair, now, now + 1_000L);
    validation = l4KeyPair.publicKeyChain.isValid(rootPublicKey, ecc);
    Assert.isTrue(validation.isValid, "l4 verify fail: " + validation);

    l2KeyPair.publicKeyChain.key.endTimestamp += 1000L;
    l2KeyPair.publicKeyChain.key.clearRaw();

    validation = l4KeyPair.publicKeyChain.isValid(rootPublicKey, ecc);
    Assert.isTrue(validation.equals(AsmPublicKeyChain.Validation.INVALID_KEY_SIGN),
                  "l4 check fail, the sign should be invalid");
  }
}
