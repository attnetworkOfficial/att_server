package org.attnetwork.proto.msg;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class ErrorMsg extends AbstractSeqLanObject {
  public String code;
  public String msg;
}
