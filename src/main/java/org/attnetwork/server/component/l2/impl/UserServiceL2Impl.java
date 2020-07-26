package org.attnetwork.server.component.l2.impl;

import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.server.component.l2.UserServiceL2;
import org.springframework.stereotype.Service;

@Service
public class UserServiceL2Impl implements UserServiceL2 {
  @Override
  public void validUserCheck(AsmPublicKeyChain keyChain) {
    byte[] root = keyChain.rootKey().data;
    // check if this key chain valid
  }
}
