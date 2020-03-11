package org.attnetwork.crypto;

import java.security.Key;

public interface Encrypt {
  EncryptedData encrypt(Key key, byte[]... data);

  byte[] decrypt(Key key, byte[] data);
}
