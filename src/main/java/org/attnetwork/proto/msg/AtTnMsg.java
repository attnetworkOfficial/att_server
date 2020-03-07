package org.attnetwork.proto.msg;

import org.attnetwork.proto.msg.comp.AppendData;
import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.attnetwork.utils.BitmapFlags;

public class AtTnMsg<
    Msg extends AbstractSeqLanObject,
    Append extends AppendData
    > extends AbstractSeqLanObject {

  public String version;
  public BitmapFlags<Flag> flags;
  public Long timestamp;
  public Msg msg;
  public Append append;

  public enum Flag {
    SIGNED,
    ENCRYPTED,
  }

  public static <M extends AbstractSeqLanObject> AtTnMsg<M, AppendData> build(M msg) {
    AtTnMsg<M, AppendData> m = new AtTnMsg<>();
    m.version = "alpha.1.0.0";
    m.flags = BitmapFlags.create(Flag.class);
    m.timestamp = System.currentTimeMillis();
    m.msg = msg;
    return m;
  }
}
