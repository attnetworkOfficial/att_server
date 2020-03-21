package org.attnetwork.crypto;

import java.security.Key;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Cipher;
import org.attnetwork.exception.AException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class AesCrypto {
  private final String algorithm;
  private final Cipher cipher;

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  public static AesCrypto instance(String mode) {
    return new AesCrypto(mode, BouncyCastleProvider.PROVIDER_NAME);
  }

  private AesCrypto(
      String algorithm,
      String provider) {
    this.algorithm = algorithm;

    try {
      this.cipher = Cipher.getInstance(algorithm, provider);
    } catch (Exception e) {
      throw AException.wrap(e);
    }
  }

  public byte[] encrypt(Key key, AlgorithmParameterSpec spec, byte[] data) {
    try {
      synchronized (cipher) {
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        return cipher.doFinal(data);
      }
    } catch (Exception e) {
      throw AException.wrap(e);
    }
  }

  public byte[] decrypt(Key key, AlgorithmParameterSpec spec, byte[] data) {
    try {
      synchronized (cipher) {
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        return cipher.doFinal(data);
      }
    } catch (Exception e) {
      throw AException.wrap(e);
    }
  }

  public int getBlockSize() {
    return cipher.getBlockSize();
  }
}
