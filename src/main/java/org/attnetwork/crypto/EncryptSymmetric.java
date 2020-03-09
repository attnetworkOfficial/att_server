package org.attnetwork.crypto;

import java.security.Key;

public interface EncryptSymmetric {
  String getSymmetricAlgorithm();

  byte[] encrypt(Key key, byte[]... data);

  byte[] decrypt(Key key, byte[] data);
}
