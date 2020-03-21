package org.attnetwork.crypto;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import org.attnetwork.crypto.asymmetric.AsmKeyPair;
import org.attnetwork.crypto.asymmetric.AsmPublicKey;
import org.attnetwork.crypto.asymmetric.AsmPublicKeyChain;
import org.attnetwork.crypto.asymmetric.AsmSignature;
import org.attnetwork.crypto.asymmetric.EncryptAsymmetric;
import org.attnetwork.exception.AException;
import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.asn1.x9.X9IntegerConverter;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.HMacDSAKCalculator;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPrivateKey;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECAlgorithms;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;

public class ECCrypto implements EncryptAsymmetric {
  private final String algorithm;
  private final String paramName;
  private final String signAlgorithm;
  private final String encryptAlgorithm;
  private final String provider;

  private final ECParameterSpec ecParameterSpec;
  private final ECDomainParameters ecDomainParameters;
  private final Signature signature;
  private final Cipher cipher;

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  public static ECCrypto instance() {
    return new ECCrypto(
        "EC",
        "secp256k1",
        "SHA256withECDSA",
        "ECIES",
        BouncyCastleProvider.PROVIDER_NAME);
  }

  private ECCrypto(
      String algorithm,
      String paramName,
      String signAlgorithm,
      String encryptAlgorithm,
      String provider) {
    this.algorithm = algorithm;
    this.paramName = paramName;
    this.provider = provider;
    this.signAlgorithm = signAlgorithm;
    this.encryptAlgorithm = encryptAlgorithm;

    X9ECParameters p = SECNamedCurves.getByName(paramName);
    this.ecDomainParameters = new ECDomainParameters(p.getCurve(), p.getG(), p.getN(), p.getH());
    this.ecParameterSpec = ECNamedCurveTable.getParameterSpec(paramName);

    try {
      this.signature = Signature.getInstance(signAlgorithm, provider);
      this.cipher = Cipher.getInstance(encryptAlgorithm, provider);
    } catch (Exception e) {
      throw AException.wrap(e);
    }
//    this.HALF_CURVE_ORDER = p.getN().shiftRight(1);
  }

  public EncryptedData encrypt(Key key, byte[] data) {
    try {
      synchronized (cipher) {
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return EncryptedData.build(encryptAlgorithm, cipher.doFinal(data));
      }
    } catch (Exception e) {
      throw AException.wrap(e);
    }
  }

  public byte[] decrypt(Key key, byte[] data) {
    try {
      synchronized (cipher) {
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
      }
    } catch (Exception e) {
      throw AException.wrap(e);
    }
  }

  @Override
  public AsmSignature sign(PrivateKey privateKey, byte[] data) {
    try {
      synchronized (signature) {
        signature.initSign(privateKey);
        signature.update(data);
        return AsmSignature.build(signAlgorithm, signature.sign());
      }
    } catch (Exception e) {
      throw AException.wrap(e);
    }
  }

  @Override
  public boolean verify(byte[] publicKey, byte[] sign, byte[] data) {
    return verify(restorePublicKey(publicKey), sign, data);
  }

  @Override
  public boolean verify(PublicKey publicKey, byte[] sign, byte[] data) {
    try {
      synchronized (signature) {
        signature.initVerify(publicKey);
        signature.update(data);
        return signature.verify(sign);
      }
    } catch (Exception e) {
      throw AException.wrap(e);
    }
  }

  @Override
  public byte[] derivePublicKey(PrivateKey privateKey) {
    ECKeyPair ecKeyPair = restoreKeyPair((BCECPrivateKey) privateKey);
    return ecKeyPair.getPublicKey().getQ().getEncoded(true);
  }

  public BigInteger[] sign(BigInteger privateKey, byte[] data) {
    ECDSASigner signer = new ECDSASigner(new HMacDSAKCalculator(new SHA256Digest()));
    signer.init(true, new ECPrivateKeyParameters(privateKey, ecDomainParameters));
    return signer.generateSignature(data);
  }

  public boolean verify(ECPoint publicKey, BigInteger[] sign, byte[] data) {
    ECDSASigner signer = new ECDSASigner();
    signer.init(false, new ECPublicKeyParameters(publicKey, ecDomainParameters));
    return signer.verifySignature(data, sign[0], sign[1]);
  }

  public boolean verify(int v, BigInteger[] sign, byte[] data) {
    return verify(recoverPublicKey(v, sign, data), sign, data);
  }

  public int generateSignV(ECPoint publicKey, BigInteger[] sign, byte[] data) {
    byte[] encoded = publicKey.getEncoded(true);
    BigInteger e = new BigInteger(1, data);
    for (int i = 0; i < 4; i++) {
      ECPoint k = recoverPublicKey(i, sign, e);
      if (k != null && Arrays.equals(k.getEncoded(true), encoded)) {
        return i;
      }
    }
    throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
  }

  private ECPoint recoverPublicKey(int v, BigInteger[] sign, byte[] data) {
    return recoverPublicKey(v, sign, new BigInteger(1, data));
  }

  private ECPoint recoverPublicKey(int v, BigInteger[] sign, BigInteger e) {
    BigInteger r = sign[0];
    BigInteger s = sign[1];
    BigInteger n = ecParameterSpec.getN();
    if (!(r.signum() > 0 && r.compareTo(n) < 0)) {
      throw new RuntimeException("Invalid r value");
    }
    if (!(s.signum() > 0 && s.compareTo(n) < 0)) {
      throw new RuntimeException("Invalid s value");
    }
    BigInteger i = BigInteger.valueOf(v >> 1);
    BigInteger x = r.add(i.multiply(n));

    ECCurve.Fp curve = (ECCurve.Fp) ecDomainParameters.getCurve();
    BigInteger prime = curve.getQ();
    if (x.compareTo(prime) >= 0) {
      throw new RuntimeException("Invalid x value");
    }

    ECPoint R = decompressKey(x, (v & 1) == 1);
    if (!R.multiply(n).isInfinity()) {
      throw new RuntimeException("nR is not a valid curve point");
    }
    BigInteger a = e.negate().mod(n);
    BigInteger b = r.modInverse(n);
    BigInteger c = b.multiply(s).mod(n);
    BigInteger d = b.multiply(a).mod(n);
    ECPoint Q = ECAlgorithms.sumOfTwoMultiplies(ecParameterSpec.getG(), d, R, c);
    if (Q.isInfinity()) {
      throw new RuntimeException("ECPoint Q is at infinity");
    }
    return Q;
  }

  private ECPoint decompressKey(BigInteger xBN, boolean yBit) {
    X9IntegerConverter x9 = new X9IntegerConverter();
    byte[] compEnc = x9.integerToBytes(xBN, 1 + x9.getByteLength(ecParameterSpec.getCurve()));
    compEnc[0] = (byte) (yBit ? 0x03 : 0x02);
    return ecParameterSpec.getCurve().decodePoint(compEnc);
  }

  public ECKeyPair generateKeyPair() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm, provider);
      generator.initialize(new ECGenParameterSpec(paramName), new SecureRandom());
      return new ECKeyPair(generator.generateKeyPair());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public AsmKeyPair generateRootKey() {
    return generateSubKey(null, AsmPublicKey.preGen());
  }

  public AsmKeyPair generateSubKey(AsmKeyPair superKeyPair, AsmPublicKey preGen) {
    ECKeyPair geneKeyPair = generateKeyPair();
    // setup validations
    AsmPublicKeyChain pubKeyChain = new AsmPublicKeyChain();
    pubKeyChain.key = preGen
        .algorithm(algorithm + "-" + paramName)
        .data(geneKeyPair.getPublicKey().getQ().getEncoded(true));
    if (superKeyPair != null) {
      if (!superKeyPair.publicKeyChain.key.isValidTimestamp()) {
        throw new AException("the timestamp of the superKeyPair.publicKey is invalid");
      }
      pubKeyChain.superKey = superKeyPair.publicKeyChain;
      pubKeyChain.makePublicKeyTimeStampReasonable();
      // sign with super key
      byte[] publicKey = pubKeyChain.key.getRaw();
      pubKeyChain.sign = sign(restorePrivateKey(superKeyPair.privateKey), publicKey);
    }
    AsmKeyPair keyPair = new AsmKeyPair();
    keyPair.privateKey = geneKeyPair.getPrivateKey().getD().toByteArray();
    keyPair.publicKeyChain = pubKeyChain;
    return keyPair;
  }

  public ECKeyPair restoreKeyPair(String hexString) {
    return restoreKeyPair(restorePrivateKey(hexString));
  }

  public ECKeyPair restoreKeyPair(BCECPrivateKey privateKey) {
    return new ECKeyPair(privateKey, restorePublicKey(ecParameterSpec.getG().multiply(privateKey.getD())));
  }

  public BCECPrivateKey restorePrivateKey(String hexString) {
    return restorePrivateKey(new BigInteger(hexString, 16));
  }

  public BCECPrivateKey restorePrivateKey(byte[] encoded) {
    return restorePrivateKey(new BigInteger(encoded));
  }

  public BCECPrivateKey restorePrivateKey(BigInteger d) {
    ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(d, ecParameterSpec);
    return new BCECPrivateKey(algorithm, ecPrivateKeySpec, BouncyCastleProvider.CONFIGURATION);
  }

  public BCECPublicKey restorePublicKey(String hexString) {
    return restorePublicKey(ByteUtils.fromHexString(hexString));
  }

  public BCECPublicKey restorePublicKey(byte[] encoded) {
    return restorePublicKey(ecParameterSpec.getCurve().decodePoint(encoded));
  }

  private BCECPublicKey restorePublicKey(ECPoint ecPoint) {
    ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(ecPoint, ecParameterSpec);
    return new BCECPublicKey(algorithm, ecPublicKeySpec, BouncyCastleProvider.CONFIGURATION);
  }
}
