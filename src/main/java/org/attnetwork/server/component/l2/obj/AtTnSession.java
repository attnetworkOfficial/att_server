package org.attnetwork.server.component.l2.obj;

import java.security.SecureRandom;
import org.attnetwork.crypto.AesCrypto;
import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.exception.AException;
import org.attnetwork.proto.attn.AtTnKeyConverter;
import org.attnetwork.proto.attn.AtTnKeys;
import org.attnetwork.proto.attn.AtTnProto;
import org.attnetwork.proto.msg.wrapper.AtTnEncryptedMsg;
import org.attnetwork.proto.msg.wrapper.AtTnOriginMsg;
import org.attnetwork.proto.sl.SeqLan;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

public class AtTnSession {
  private static final SecureRandom secureRandom = new SecureRandom();

  private Integer id;
  private AtTnProto proto;
  private AtTnKeyConverter atTnKeyConverter;
  private AsmPublicKeyChain userPublicKeyChain;
  private long saltTimestamp;
  private long createTimestamp;
  private long lastActiveTime;
  private AesCrypto cipher;
  private byte[] salt;
  private byte[] oldSalt;

  public void init(
      Integer id,
      AtTnProto proto,
      byte[] clientRandom,
      byte[] serverRandom,
      AsmPublicKeyChain userPublicKeyChain) {
    if (this.id != null) {
      throw new AException("session has already been initialized");
    }
    this.id = id;
    this.proto = proto;
    this.atTnKeyConverter = proto.AES_KEY_CONVERTER.init(clientRandom, serverRandom);
    this.userPublicKeyChain = userPublicKeyChain;
    this.createTimestamp = System.currentTimeMillis();
    this.saltTimestamp = 0L;
    this.cipher = AesCrypto.instance(atTnKeyConverter.aesMode());
    refresh();
  }

  private void refresh() {
    refreshSessionSalt();
    active();
  }

  private void refreshSessionSalt() {
    long now = System.currentTimeMillis();
    if (saltTimestamp < now) {
      byte[] newSalt = new byte[getProto().SERVER_SALT_SIZE];
      secureRandom.nextBytes(newSalt);
      oldSalt = salt;
      salt = newSalt;
      saltTimestamp = now + getProto().SERVER_SALT_UPDATE_TIME;
    }
  }

  public AtTnEncryptedMsg encrypt(byte[] data) {
    refresh();
    //
    AtTnOriginMsg originMsg = new AtTnOriginMsg();
    originMsg.data = data;
    originMsg.salt = salt;
    originMsg.timestamp = System.currentTimeMillis();
    padOriginMsg(originMsg);
    //
    AtTnKeys keys = atTnKeyConverter.keysFromRaw(originMsg.getRaw());
    byte[] encryptedData = cipher.encrypt(keys.aesKey, keys.aesIv, originMsg.getRaw());
    AtTnEncryptedMsg encryptedMsg = new AtTnEncryptedMsg();
    encryptedMsg.sessionId = id;
    encryptedMsg.data = encryptedData;
    encryptedMsg.msgKey = keys.msgKey;
    return encryptedMsg;
  }

  public AtTnOriginMsg decrypt(AtTnEncryptedMsg encryptedMsg) {
    AtTnKeys keys = atTnKeyConverter.keysFromMsgKey(encryptedMsg.msgKey);
    byte[] decryptedData = cipher.decrypt(keys.aesKey, keys.aesIv, encryptedMsg.data);
    AtTnOriginMsg originMsg = AtTnOriginMsg.read(decryptedData, AtTnOriginMsg.class);
    // check
    if (!ByteUtils.equals(atTnKeyConverter.msgKeyFromRaw(decryptedData), encryptedMsg.msgKey)) {
      throw new AException("msg-key doesn't match");
    }
    if (!ByteUtils.equals(salt, originMsg.salt)) {
      if (oldSalt == null || !ByteUtils.equals(oldSalt, originMsg.salt)) {
        throw new AException("msg-salt doesn't match");
      }
    }
    active();
    return originMsg;
  }

  private void padOriginMsg(AtTnOriginMsg originMsg) {
    int random = secureRandom.nextInt(16);
    byte[] raw = originMsg.getRaw();
    int len = raw.length;
    int b = atTnKeyConverter.aesBlockSize() / 8;
    int pad;
    int padLen;
    while (true) {
      pad = b - len % b + b * random;
      padLen = SeqLan.varIntLength(pad);
      if (padLen == SeqLan.varIntLength(pad - padLen)) {
        pad -= padLen;
        break;
      }
      ++random;
    }
    originMsg.padding = new byte[pad];
    secureRandom.nextBytes(originMsg.padding);
    originMsg.clearRaw();
  }

  private void active() {
    lastActiveTime = System.currentTimeMillis();
  }

  public Integer getId() {
    return id;
  }

  public AtTnProto getProto() {
    return proto;
  }

  public byte[] getSalt() {
    return salt;
  }
}
