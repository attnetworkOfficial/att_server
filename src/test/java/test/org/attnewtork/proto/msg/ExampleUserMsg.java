package test.org.attnewtork.proto.msg;

import java.math.BigDecimal;
import org.attnetwork.proto.sl.AbstractSeqLanObject;

public class ExampleUserMsg extends AbstractSeqLanObject {
  public Long id;
  public String firstName;
  public String lastName;
  public BigDecimal score;
  public String phone;
  @ProcessFieldData
  public ExampleUserContactMsg[] contacts;
}
