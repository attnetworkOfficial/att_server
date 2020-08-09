package org.attnetwork.proto.msg;

import org.attnetwork.proto.sl.AbstractSeqLanObject;

public class ForwardFromMsg extends AbstractSeqLanObject {
  public UserMsg fromUser;
  private ChatMsg fromChat;
  private Integer fromMessageId;
  private String signature;
  private String senderName;
  private Integer forwardTime;
}
