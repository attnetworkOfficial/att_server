package test.org.tools;

import org.attnetwork.crypto.ECCrypto;
import org.attnetwork.crypto.asymmetric.AsmKeyPair;
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
    System.out.println("generate root key");
    AsmKeyPair rootKeyPair = ecc.generateRootKey();
    System.out.println("   hex: " + rootKeyPair.toHexString());
    System.out.println("base64: " + rootKeyPair.toBase64String());
    System.out.println(rootKeyPair.publicKeyChain);
  }

  // generate root key
  //   hex: 5c21008bce8ebfd66ca0161b5336c98d13aed3f540b1194ea0b31d953ea25635398dd139360c45432d736563703235366b31000004646573632102bf4e59b8c6a3c48514d53e745f98bfc7e52efc35dbd166706f157ffcfdc506650000
  //base64: XCEAi86Ov9ZsoBYbUzbJjROu0/VAsRlOoLMdlT6iVjU5jdE5NgxFQy1zZWNwMjU2azEAAARkZXNjIQK/Tlm4xqPEhRTVPnRfmL/H5S78NdvRZnBvFX/8/cUGZQAA
  //---- public key info ----
  //algorithm:      EC-secp256k1
  //invalid before: null
  //invalid after:  null
  //description:    desc
  //key:            02bf4e59b8c6a3c48514d53e745f98bfc7e52efc35dbd166706f157ffcfdc50665

  @Test
  void generateL2Key() {
    System.out.println("generate L2 key");
    String rootKey = "XCEAi86Ov9ZsoBYbUzbJjROu0/VAsRlOoLMdlT6iVjU5jdE5NgxFQy1zZWNwMjU2azEAAARkZXNjIQK/Tlm4xqPEhRTVPnRfmL/H5S78NdvRZnBvFX/8/cUGZQAA";
    AsmKeyPair rootKeyPair = AsmKeyPair.readBase64String(rootKey, AsmKeyPair.class);
    Long now = System.currentTimeMillis();
    AsmKeyPair l2KeyPair = ecc.generateSubKey(rootKeyPair, now, now + 86_400_000L * 7L);
    System.out.println("   hex: " + l2KeyPair.toHexString());
    System.out.println("base64: " + l2KeyPair.toBase64String());
    System.out.println(l2KeyPair.publicKeyChain);
  }

  /*
generate L2 key
   hex: 817a20222c7ecdad0ba156221bd85ad9ed8ed8b4945dbfa695bd70581fb3e007f09281815743420c45432d736563703235366b31060170d9797314060170fd85f71404646573632103114631b95007376f492faa0076d4b689bf463f3e685a39c786b5f5d3d728e61b580f5348413235367769746845434453414730450220462fcb63d7fdb98f40955622b5a235777c0b0ff0e5388938662d8938c07b963f02210083c0e0e03129b2d8cf42dda7f8902fc004593a7afe7a05f4f7ec29b5130278c639360c45432d736563703235366b31000004646573632102bf4e59b8c6a3c48514d53e745f98bfc7e52efc35dbd166706f157ffcfdc506650000
base64: gXogIix+za0LoVYiG9ha2e2O2LSUXb+mlb1wWB+z4AfwkoGBV0NCDEVDLXNlY3AyNTZrMQYBcNl5cxQGAXD9hfcUBGRlc2MhAxFGMblQBzdvSS+qAHbUtom/Rj8+aFo5x4a19dPXKOYbWA9TSEEyNTZ3aXRoRUNEU0FHMEUCIEYvy2PX/bmPQJVWIrWiNXd8Cw/w5TiJOGYtiTjAe5Y/AiEAg8Dg4DEpstjPQt2n+JAvwARZOnr+egX09+wptRMCeMY5NgxFQy1zZWNwMjU2azEAAARkZXNjIQK/Tlm4xqPEhRTVPnRfmL/H5S78NdvRZnBvFX/8/cUGZQAA
---- public key info ----
algorithm:      EC-secp256k1
invalid before: null
invalid after:  null
description:    desc
key:            02bf4e59b8c6a3c48514d53e745f98bfc7e52efc35dbd166706f157ffcfdc50665

---- public key info ----
algorithm:      EC-secp256k1
invalid before: 2020-03-14T14:36:20.116Z
invalid after:  2020-03-21T14:36:20.116Z
description:    desc
key:            03114631b95007376f492faa0076d4b689bf463f3e685a39c786b5f5d3d728e61b
---- signature info ----
 algorithm: SHA256withECDSA
 hex:       30450220462fcb63d7fdb98f40955622b5a235777c0b0ff0e5388938662d8938c07b963f02210083c0e0e03129b2d8cf42dda7f8902fc004593a7afe7a05f4f7ec29b5130278c6   */

  @Test
  void generateL3Key() {
    System.out.println("generate L3 key");
    String L2Key = "817a20222c7ecdad0ba156221bd85ad9ed8ed8b4945dbfa695bd70581fb3e007f09281815743420c45432d736563703235366b31060170d9797314060170fd85f71404646573632103114631b95007376f492faa0076d4b689bf463f3e685a39c786b5f5d3d728e61b580f5348413235367769746845434453414730450220462fcb63d7fdb98f40955622b5a235777c0b0ff0e5388938662d8938c07b963f02210083c0e0e03129b2d8cf42dda7f8902fc004593a7afe7a05f4f7ec29b5130278c639360c45432d736563703235366b31000004646573632102bf4e59b8c6a3c48514d53e745f98bfc7e52efc35dbd166706f157ffcfdc506650000";
    AsmKeyPair l2KeyPair = AsmKeyPair.readHexString(L2Key, AsmKeyPair.class);
    Long now = System.currentTimeMillis();
    AsmKeyPair l3KeyPair = ecc.generateSubKey(l2KeyPair, now, now + 86_400_000L);
    System.out.println("base64: " + l3KeyPair.toBase64String());
    System.out.println(l3KeyPair.publicKeyChain);
  }
}
