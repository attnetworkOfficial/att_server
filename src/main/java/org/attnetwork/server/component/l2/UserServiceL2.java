package org.attnetwork.server.component.l2;

import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;

public interface UserServiceL2 {
  void validUserCheck(AsmPublicKeyChain keyChain);
}
