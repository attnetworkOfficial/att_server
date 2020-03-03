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
import org.springframework.util.Assert;

class ECCryptoTest {
  private static ECCrypto ecc = ECCrypto.instance();

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
    int round = 100;
    CountDownLatch countDownLatch = new CountDownLatch(round * 3);
    new Thread(() -> testSignVerify(round, countDownLatch)).start();
    new Thread(() -> testSignVerify(round, countDownLatch)).start();
    testSignVerify(round, countDownLatch);

    countDownLatch.await();
  }

  private void testSignVerify(int round, CountDownLatch countDownLatch) {
    for (int i = 0; i < round; i++) {
      ECKeyPair pair = ecc.generateKeyPair();
      BCECPrivateKey privateKey = pair.getPrivateKey();
      BCECPublicKey publicKey = pair.getPublicKey();
      byte[] data = HashUtil.sha256(("asdfafds" + i).getBytes());
      BigInteger[] sign = ecc.sign(privateKey.getD(), data);
      boolean verify = ecc.verify(publicKey.getQ(), sign, data);
      Assert.isTrue(verify, "verify ecc sign fail");
      int v = ecc.generateSignV(publicKey.getQ(), sign, data);
      verify = ecc.verify(v, sign, data);
      Assert.isTrue(verify, "verify ecc from recovery public key fail");
      countDownLatch.countDown();
    }
    System.out.println("test sign verify finish");
  }
}
