package test.org.attnetwork.crypto;

import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import org.attnetwork.crypto.ECCrypto;
import org.attnetwork.crypto.ECKeyPair;
import org.attnetwork.utils.HashUtil;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

class ECCryptoTest {
  private Logger logger = LoggerFactory.getLogger(getClass());
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


      byte[] sign2 = ecc.sign(privateKey, data);
      logger.debug("r:{} s:{}\ns2:{}", sign[0].toString(16), sign[1].toString(16), ByteUtils.toHexString(sign2));

      int v = ecc.generateSignV(publicKey.getQ(), sign, data);
      verify = ecc.verify(v, sign, data);
      Assert.isTrue(verify, "verify ecc from recovery public key fail\n" + info);
      countDownLatch.countDown();
    }
    logger.trace("test sign verify finish");
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
      byte[] sign = ecc.sign(privateKey, data);
      boolean verify = ecc.verify(publicKey, sign, data);
      Assert.isTrue(verify, "verify ecc sign fail\n" + info);

      countDownLatch.countDown();
    }
    logger.trace("test sign verify finish");
  }



  @Test
  void testCrypt() {
    ECKeyPair pair = ecc.generateKeyPair();
    BCECPrivateKey privateKey = pair.getPrivateKey();
    BCECPublicKey publicKey = pair.getPublicKey();

    byte[] origin = new byte[10000];
    byte[] encrypt = ecc.encrypt(publicKey, origin);
    System.out.println(encrypt.length);
    byte[] decrypt = ecc.decrypt(privateKey, encrypt);
    System.out.println(decrypt.length);

    Assert.isTrue(ByteUtils.equals(origin, decrypt), "fail");
  }
}
