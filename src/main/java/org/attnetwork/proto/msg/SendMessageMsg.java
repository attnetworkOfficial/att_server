package org.attnetwork.proto.msg;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public final class SendMessageMsg extends AbstractSeqLanObject {
  public Integer id;
  public Integer chatId;
  public Integer timestamp;
  public ChatMsg chat;
  public ForwardFromMsg forwardFrom;
  public SendMessageMsg replayTo;
}
