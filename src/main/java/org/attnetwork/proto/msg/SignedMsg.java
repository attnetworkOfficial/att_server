package org.attnetwork.proto.msg;

import java.math.BigInteger;
import org.attnetwork.proto.sl.AbstractMsg;

public class SignedMsg<T extends AbstractMsg> extends AbstractMsg {
  public T msg;
  public BigInteger[] sign;
  public byte[] publicKey;

  public void sign() {

  }
}
