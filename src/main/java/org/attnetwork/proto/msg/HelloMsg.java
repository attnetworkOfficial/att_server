package org.attnetwork.proto.msg;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.attnetwork.proto.sl.AbstractMsg;

public class HelloMsg extends AbstractMsg {
  public Integer a;
  public Long b;
  public BigInteger[] sign;
  public BigDecimal decimal;
}
