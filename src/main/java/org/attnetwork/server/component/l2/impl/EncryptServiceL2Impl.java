package org.attnetwork.server.component.l2.impl;

import org.attnetwork.crypto.ECCrypto;
import org.attnetwork.server.component.l2.EncryptServiceL2;
import org.springframework.stereotype.Service;

@Service
public class EncryptServiceL2Impl implements EncryptServiceL2 {

  private final ECCrypto ecc = ECCrypto.instance();

  public byte[] sign(byte[] data) {
    return null;
  }
}
