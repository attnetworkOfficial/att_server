package test.org.attnetwork.proto.sl.msg;

import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.attnetwork.utils.BitmapFlags;

public class ExampleChatMsg extends AbstractSeqLanObject {
  public Integer id;
  public BitmapFlags<Flags> flags;

  public enum Flags {
    IS_CREATOR,
    IS_KICKED,
    IS_LEFT,
    IS_DEACTIVATED,
  }
}
