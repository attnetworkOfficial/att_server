package org.attnetwork.proto.attn;

import static org.attnetwork.utils.ByteUtil.subArray;
import static org.attnetwork.utils.HashUtil.SHA3_256;
import static org.attnetwork.utils.HashUtil.SHA3_512;

import java.util.HashMap;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AtTnProto {
  public static final AtTnProto DEBUG = new AtTnProto(
      "debug",
      8,
      1000,
      64,
      defaultAesConverter());

  public static final AtTnProto V1_0 = new AtTnProto(
      "1.0",
      8,
      30 * (60 * 1000),
      64,
      defaultAesConverter());

  public static AtTnProto getByVersion(String version) {
    return MAP.get(version);
  }

  private static final HashMap<String, AtTnProto> MAP = new HashMap<>();

  static {
    MAP.put("1.0", V1_0);
    MAP.put("debug", DEBUG);
  }


  private AtTnProto(
      String VERSION,
      int SERVER_SALT_SIZE,
      int SERVER_SALT_UPDATE_TIME,
      int SESSION_START_RANDOM_SIZE,
      AesKeyConverter AES_KEY_CONVERTER) {

    this.VERSION = VERSION;
    this.SERVER_SALT_SIZE = SERVER_SALT_SIZE;
    this.SESSION_START_RANDOM_SIZE = SESSION_START_RANDOM_SIZE;
    this.SERVER_SALT_UPDATE_TIME = SERVER_SALT_UPDATE_TIME;
    this.AES_KEY_CONVERTER = AES_KEY_CONVERTER;
  }

  public final String VERSION;
  public final int SERVER_SALT_SIZE; //bytes
  public final int SESSION_START_RANDOM_SIZE; // bytes;
  public final long SERVER_SALT_UPDATE_TIME; // ms
  public final AesKeyConverter AES_KEY_CONVERTER;


  private static AesKeyConverter defaultAesConverter() {
    return (rand1, rand2) -> {
      AtTnKeyConverter c = new AtTnKeyConverter() {
        @Override
        public String aesMode() {
          return "AES/GCM/NoPadding";
        }

        @Override
        public int aesBlockSize() {
          return 256;
        }

        @Override
        public byte[] msgKeyFromRaw(byte[] raw) {
          return subArray(SHA3_256.hash(msgKeyPart1, raw, msgKeyPart2), 4, 16);
        }

        @Override
        public AtTnKeys keysFromRaw(byte[] raw) {
          return keysFromMsgKey(msgKeyFromRaw(raw));
        }

        @Override
        public AtTnKeys keysFromMsgKey(byte[] msgKey) {
          AtTnKeys k = new AtTnKeys();
          k.msgKey = msgKey;
          k.aesKey = new SecretKeySpec(SHA3_256.hash(msgKey, aesKeyPart1), "AES");
          k.aesIv = new GCMParameterSpec(128, SHA3_256.hash(msgKey, aesKeyPart2));
          return k;
        }
      };
      byte[] sharedSecret1 = SHA3_512.hash(rand1, rand2);
      byte[] sharedSecret2 = SHA3_512.hash(rand2, rand1);
      c.msgKeyPart1 = subArray(sharedSecret1, 0, 32);
      c.aesKeyPart1 = subArray(sharedSecret1, 32, 32);
      c.msgKeyPart2 = subArray(sharedSecret2, 0, 32);
      c.aesKeyPart2 = subArray(sharedSecret2, 32, 32);
      return c;
    };
  }

  public interface AesKeyConverter {
    AtTnKeyConverter convert(byte[] rand1, byte[] rand2);
  }
}
