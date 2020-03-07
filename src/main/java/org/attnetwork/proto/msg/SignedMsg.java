package org.attnetwork.proto.msg;

import java.security.PrivateKey;
import java.security.PublicKey;
import org.attnetwork.crypto.EncryptAsymmetric;
import org.attnetwork.proto.sl.AbstractSeqLanObject;

public class SignedMsg<Msg extends AbstractSeqLanObject> extends AbstractSeqLanObject {
//  public Msg msg;
//  public Encryption enc;
//
//  public SignedMsg() {
//  }
//
//  public SignedMsg(Msg msg) {
//    this.msg = msg;
//  }
//
//  public static <Msg extends AbstractSeqLanObject> SignedMsg<Msg> doSign(
//      Msg msg, PrivateKey privateKey, EncryptAsymmetric encryptAsymmetric) {
//    SignedMsg<Msg> signedMsg = new SignedMsg<>(msg);
//    signedMsg.sign(privateKey, encryptAsymmetric);
//    return signedMsg;
//  }
//
//  private void sign(PrivateKey privateKey, EncryptAsymmetric encryptAsymmetric) {
//    enc = new Encryption();
//    enc.algorithm = encryptAsymmetric.getAsymmetricAlgorithm();
//    enc.sign = encryptAsymmetric.sign(privateKey, getRaw(msg));
//    enc.publicKey = encryptAsymmetric.derivePublicKey(privateKey);
//  }
//
//  public boolean verify(PublicKey publicKey, EncryptAsymmetric encryptAsymmetric) {
//    return encryptAsymmetric.verify(publicKey, enc.sign, getRaw(msg));
//  }
}
