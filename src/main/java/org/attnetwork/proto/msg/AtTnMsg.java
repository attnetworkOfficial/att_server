package org.attnetwork.proto.msg;

import org.attnetwork.proto.sl.AbstractSeqLanObject;
import org.attnetwork.utils.BitmapFlags;

public class AtTnMsg<Msg extends AbstractSeqLanObject> extends AbstractSeqLanObject {
  static final String MSG_VERSION = "alpha.1.0.0";

  public String version;
  public BitmapFlags<MsgFlag> flags;
  public Long timestamp;
  @ProcessFieldData
  public Msg msg;

  private byte[] originRaw;

  public static <M extends AbstractSeqLanObject> AtTnMsg<M> build(M msg) {
    AtTnMsg<M> m = new AtTnMsg<>();
    m.version = MSG_VERSION;
    m.flags = BitmapFlags.create(MsgFlag.class);
    m.timestamp = System.currentTimeMillis();
    m.msg = msg;
    return m;
  }

  public void sign() {

  }

  private byte[] getOriginRaw() {
    if (originRaw == null) {
      originRaw = getRaw();
    }
    return originRaw;
  }
}
