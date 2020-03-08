package org.attnetwork.crypto;

import java.security.Key;

public interface EncryptSymmetric {
  byte[] encrypt(Key key, byte[]... data);

  byte[] decrypt(Key key, byte[] data);
}
