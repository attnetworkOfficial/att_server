package test.org.tools;

import org.attnetwork.crypto.ECCrypto;
import org.attnetwork.crypto.asymmetric.AsmKeyPair;
import org.attnetwork.crypto.asymmetric.AsmPublicKey;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.event.annotation.BeforeTestClass;

public class KeyGenerator {
  private ECCrypto ecc = ECCrypto.instance();

  @BeforeTestClass
  public void beforeClass() {
    Assumptions.assumeTrue(false);
  }

  @Test
  void generateRootKey() {
    System.out.println("============ generate root key ============");
    AsmKeyPair rootKeyPair = ecc.generateRootKey();
    rootKeyPair.publicKeyChain.key.desc("TEST-ROOT");
    System.out.println("base64: " + rootKeyPair.toBase64String());
    System.out.println(rootKeyPair.publicKeyChain);
  }

  /*
base64: aCBYJlDWM/L7K1PVwPQmg2iKE6CfmMqyniACL4n/9A3ECUZDDEVDLXNlY3AyNTZrMQAABgFw3N+DzQAJVEVTVC1ST09UIQJGOQlnWCxlbWSIK6zfG6oPnii3UTXR53xejiNYEgL5sgAA
---- public key info ----
algorithm:      EC-secp256k1
create time:    2020-03-15T06:26:40.717Z
description:    TEST-ROOT
key:            0246390967582c656d64882bacdf1baa0f9e28b75135d1e77c5e8e23581202f9b2
   */

  @Test
  void generateL2Key() {
    System.out.println("============ generate L2 key ============");
    String rootKey = "aCBYJlDWM/L7K1PVwPQmg2iKE6CfmMqyniACL4n/9A3ECUZDDEVDLXNlY3AyNTZrMQAABgFw3N+DzQAJVEVTVC1ST09UIQJGOQlnWCxlbWSIK6zfG6oPnii3UTXR53xejiNYEgL5sgAA";
    AsmKeyPair rootKeyPair = AsmKeyPair.readBase64String(rootKey, AsmKeyPair.class);
    Long now = System.currentTimeMillis();
    AsmKeyPair l2KeyPair = ecc.generateSubKey(rootKeyPair, AsmPublicKey.preGen().start(now).end(now + 86_400_000L * 7L));
    System.out.println("base64: " + l2KeyPair.toBase64String());
    System.out.println(l2KeyPair.publicKeyChain);
  }

  /*
base64: ggshALUVXnzPTgJur1laNEzrVVEgFmm1tHZG3rlfBftjFlk+gWdGDEVDLXNlY3AyNTZrMQYBcNzf7P8GAXEA7HD/BgFw3N/s/wAAIQKfDGEyjy93IbCUYSENzzvo9mhidMvWeFEnAGStOGJ151gPU0hBMjU2d2l0aEVDRFNBRzBFAiAR4mbtsFS9wZMsQRHjDs+n1KyLz0VkQ3P3UA+/KiW56QIhAK3uAXXG766XBiM7ZznBMcplkv6Py7du1LijDrWzX+OnRkMMRUMtc2VjcDI1NmsxAAAGAXDc34PNAAlURVNULVJPT1QhAkY5CWdYLGVtZIgrrN8bqg+eKLdRNdHnfF6OI1gSAvmyAAA=
---- public key info ----
algorithm:      EC-secp256k1
invalid before: 2020-03-15T06:27:07.647Z
invalid after:  2020-03-22T06:27:07.647Z
create time:    2020-03-15T06:27:07.647Z
key:            029f0c61328f2f7721b09461210dcf3be8f6686274cbd67851270064ad386275e7
---- signature ----
algorithm: SHA256withECDSA
 hex:      3045022011e266edb054bdc1932c4111e30ecfa7d4ac8bcf45644373f7500fbf2a25b9e9022100adee0175c6efae9706233b6739c131ca6592fe8fcbb76ed4b8a30eb5b35fe3a7
<super key>
---- public key info ----
algorithm:      EC-secp256k1
create time:    2020-03-15T06:26:40.717Z
description:    TEST-ROOT
key:            0246390967582c656d64882bacdf1baa0f9e28b75135d1e77c5e8e23581202f9b2
  */

  @Test
  void generateL3Key() {
    System.out.println("============ generate L3 key ============");
    String L2Key = "ggshALUVXnzPTgJur1laNEzrVVEgFmm1tHZG3rlfBftjFlk+gWdGDEVDLXNlY3AyNTZrMQYBcNzf7P8GAXEA7HD/BgFw3N/s/wAAIQKfDGEyjy93IbCUYSENzzvo9mhidMvWeFEnAGStOGJ151gPU0hBMjU2d2l0aEVDRFNBRzBFAiAR4mbtsFS9wZMsQRHjDs+n1KyLz0VkQ3P3UA+/KiW56QIhAK3uAXXG766XBiM7ZznBMcplkv6Py7du1LijDrWzX+OnRkMMRUMtc2VjcDI1NmsxAAAGAXDc34PNAAlURVNULVJPT1QhAkY5CWdYLGVtZIgrrN8bqg+eKLdRNdHnfF6OI1gSAvmyAAA=";
    AsmKeyPair l2KeyPair = AsmKeyPair.readBase64String(L2Key, AsmKeyPair.class);
    Long now = System.currentTimeMillis();
    AsmKeyPair l3KeyPair = ecc.generateSubKey(l2KeyPair, AsmPublicKey.preGen().start(now).end(now + 86_400_000L));
    System.out.println("base64: " + l3KeyPair.toBase64String());
    System.out.println(l3KeyPair.publicKeyChain);
  }
}
