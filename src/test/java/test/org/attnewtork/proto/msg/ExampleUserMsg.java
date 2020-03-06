package test.org.attnewtork.proto.msg;

import java.math.BigDecimal;
import org.attnetwork.proto.sl.AbstractMsg;

public class ExampleUserMsg extends AbstractMsg {
  public Long id;
  public String firstName;
  public String lastName;
  public BigDecimal score;
  public String phone;
  public ExampleUserContactMsg[] contacts;
}
