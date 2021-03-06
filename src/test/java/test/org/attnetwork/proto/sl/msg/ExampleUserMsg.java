package test.org.attnetwork.proto.sl.msg;

import java.math.BigDecimal;
import org.attnetwork.proto.sl.AbstractSeqLanObject;

public class ExampleUserMsg extends AbstractSeqLanObject {
  public Long id;
  public String firstName;
  public String lastName;
  public BigDecimal score;
  public String phone;
  public ExampleUserContactMsg[] contacts;
}
