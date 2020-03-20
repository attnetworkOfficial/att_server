package org.attnetwork.proto.msg;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class SessionStartMsg extends AbstractSeqLanObject {
  public String attnVersion;
  public byte[] random;
}
