package org.attnetwork.utils;

import org.attnetwork.crypto.ECCrypto;
import org.attnetwork.crypto.asymmetric.AsmKeyPair;
import org.attnetwork.crypto.asymmetric.AsmPublicKey;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyGenerator {
  public static void main(String[] args) {
    ECCrypto ecc = ECCrypto.instance();
    System.out.println("============ generate key-chain ============");
    System.out.println("\uD83D\uDD10 input the super key-chain IF NEED:");
    Scanner scanner = new Scanner(System.in);
    String input = scanner.nextLine();
    AsmKeyPair keyPair;
    if (input.trim().length() == 0) {
      keyPair = ecc.generateRootKey();
    } else {
      AsmKeyPair superKeyPair = AsmKeyPair.readBase64String(input.trim(), AsmKeyPair.class);
      if (!superKeyPair.publicKeyChain.key.isValidTimestamp()) {
        System.err.println("the timestamp of the input key is invalid");
        return;
      }
      System.out.println("\uD83D\uDD52 input the valid time for this key:<number [unit]>, unit can be sec/s, min/m, hour/h(default), day/d (example: 7d)");
      while (true) {
        input = scanner.nextLine();
        Pattern p = Pattern.compile("^(\\d+) *(\\w*)$");
        Matcher m = p.matcher(input);
        if (m.find()) {
          long valid = Long.parseLong(m.group(1)) * 1000L;
          switch (m.group(2)) {
            case "min":
            case "m":
              valid *= 60;
              break;
            case "hour":
            case "h":
            case "":
              valid *= 60 * 60;
              break;
            case "day":
            case "d":
              valid *= 60 * 60 * 24;
              break;
            case "sec":
            case "s":
              break;
            default:
              System.err.println("unit is not correct: " + m.group(2));
              continue;
          }
          long now = System.currentTimeMillis();
          keyPair = ecc.generateSubKey(superKeyPair, AsmPublicKey.preGen().start(now).end(now + valid));
          break;
        } else {
          System.err.println("input format must be:<number [unit]>, unit can be sec/s, min/m, hour/h(default), day/d");
        }
      }
    }
    System.out.println(keyPair.publicKeyChain);
    System.out.println("\n┌────────────── base64 format keyPair \uD83D\uDD10 ──────────────\n\n" + keyPair.toBase64String() +
        "\n\n└──────────────────────────────────────────────────────");
  }
}
