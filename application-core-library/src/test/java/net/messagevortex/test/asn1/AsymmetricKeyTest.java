package net.messagevortex.test.asn1;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AlgorithmParameter;
import net.messagevortex.asn1.AsymmetricKey;
import net.messagevortex.asn1.encryption.Algorithm;
import net.messagevortex.asn1.encryption.AlgorithmType;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.asn1.encryption.EllipticCurveType;
import net.messagevortex.asn1.encryption.Mode;
import net.messagevortex.asn1.encryption.Padding;
import net.messagevortex.asn1.encryption.Parameter;
import net.messagevortex.asn1.encryption.SecurityLevel;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;


/**
 * Tests for AsymmetricKey class.
 * <p>
 * Created by martin.gwerder on 31.05.2016.
 */
@ExtendWith(GlobalJunitExtension.class)
public class AsymmetricKeyTest {

  private static final java.util.logging.Logger LOGGER;
  private static final Random sr = new Random();

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  private int ksDisc = 16384;

  private void asymmetricEncryptionTestRun(Algorithm alg, Padding pad, Mode m, int keySize, int payloadSize) {
    LOGGER.log(Level.INFO, "Testing " + alg + "/" + keySize + "/" + m + "/" + pad + "/" + payloadSize);
    LOGGER.log(Level.INFO, "  creating key");
    AsymmetricKey as = null;
    try {
      AlgorithmParameter p = alg.getParameters(SecurityLevel.LOW);
      p.put(Parameter.KEYSIZE.getId(), "" + keySize);
      p.put(Parameter.BLOCKSIZE.getId(), "" + keySize);
      p.put(Parameter.PADDING.getId(), pad.toString());
      p.put(Parameter.MODE.getId(), m.toString());
      if(p.get(Parameter.CURVETYPE)!=null) {
        p.put(Parameter.CURVETYPE.getId(), EllipticCurveType.getByKeySize(keySize)[0].toString());
      }
      as = new AsymmetricKey(p);
    } catch (IOException ioe) {
      LOGGER.log(Level.WARNING, "unexpected exception", ioe);
      Assertions.fail("Constructor threw IOException");
    }
    byte[] b1 = new byte[payloadSize];
    as.setMode(m);
    sr.nextBytes(b1);
    LOGGER.log(Level.INFO, "  doing an encrypt/decrypt cycle with " + payloadSize + " bytes and padding " + pad + " (max:" + as.getPadding().getMaxSize(keySize) + "; key size:" + keySize + "; pad: " + as.getPadding() + "; mode: " + as.getMode() + ")");
    byte[] b2 = null;
    byte[] b3 = null;
    try {
      b2 = as.encrypt(b1);
      b3 = as.decrypt(b2);
    } catch (IOException ioe) {
      LOGGER.log(Level.WARNING, "unexpected exception", ioe);
      Assertions.fail("IOException while reencrypting");
    }
    Assertions.assertTrue(Arrays.equals(b1, b3), "error in encrypt/decrypt cycle with " + alg + " (same object)");
    LOGGER.log(Level.INFO, "  doing an encrypt/decrypt cycle with a reencoded key");
    try {
      b3 = (new AsymmetricKey(as.toBytes(DumpType.ALL_UNENCRYPTED))).decrypt(b2);
    } catch (IOException ioe) {
      LOGGER.log(Level.WARNING, "unexpected exception", ioe);
      Assertions.fail("Constructor threw IOException");
    }
    Assertions.assertTrue(Arrays.equals(b1, b3), "error in encrypt/decrypt cycle with " + alg + " (same reserialized object)");
  }

  private void asymmetricEncryptionTestRun(Algorithm alg, int size) {
    for (Mode m : Mode.getModes(alg)) {
      LOGGER.log(Level.INFO, "Testing " + alg + "/" + size + "/" + m);
      LOGGER.log(Level.INFO, "  creating key");
      AsymmetricKey s = null;
      try {
        AlgorithmParameter p = alg.getParameters(SecurityLevel.LOW);
        p.put(Parameter.KEYSIZE.getId(), "" + size);
        p.put(Parameter.BLOCKSIZE.getId(), "" + size);
        if(p.get(Parameter.CURVETYPE)!=null) {
          p.put(Parameter.CURVETYPE,EllipticCurveType.getByKeySize(size)[0].toString());
        }
        s = new AsymmetricKey(p);
      } catch (IOException ioe) {
        LOGGER.log(Level.WARNING, "unexpected exception", ioe);
        Assertions.fail("Constructor threw IOException");
      }
      byte[] b1 = new byte[0];
      while (b1.length < 10) {
        b1 = new byte[sr.nextInt(Math.min(s.getPadding().getMaxSize(size), 1024))];
      }
      s.setMode(m);
      sr.nextBytes(b1);
      LOGGER.log(Level.INFO, "  doing an encrypt/decrypt cycle with " + b1.length + " bytes (max:" + s.getPadding().getMaxSize(size) + "; size:" + size + ")");
      byte[] b2 = null;
      byte[] b3 = null;
      try {
        b2 = s.encrypt(b1);
        b3 = s.decrypt(b2);
      } catch (IOException ioe) {
        LOGGER.log(Level.WARNING, "unexpected exception", ioe);
        Assertions.fail("IOException while reencrypting");
      }
      Assertions.assertTrue(Arrays.equals(b1, b3), "error in encrypt/decrypt cycle with " + alg + " (same object)");
      LOGGER.log(Level.INFO, "  doing an encrypt/decrypt cycle with a reencoded key");
      try {
        b3 = (new AsymmetricKey(s.toBytes(DumpType.ALL_UNENCRYPTED))).decrypt(b2);
      } catch (IOException ioe) {
        LOGGER.log(Level.WARNING, "unexpected exception", ioe);
        Assertions.fail("Constructor threw IOException");
      }
      Assertions.assertTrue(Arrays.equals(b1, b3), "error in encrypt/decrypt cycle with " + alg + " (same reserialized object)");
      for (Algorithm a : Algorithm.getAlgorithms(AlgorithmType.HASHING)) {
        b1 = new byte[sr.nextInt(4096) + 2048];
        sr.nextBytes(b1);
        byte[] sig = null;
        try {
          sig = s.sign(b1, a);
        } catch (IOException ioe) {
          LOGGER.log(Level.WARNING, "unexpected exception", ioe);
          Assertions.fail("Constructor threw IOException");
        }
        try {
          LOGGER.log(Level.INFO, "  signing with " + a + " (signature size:" + sig.length + "; message size:" + b1.length + ")");
          Assertions.assertTrue(s.verify(b1, sig, a), "error in signature verification " + a + "With" + alg + "");
        } catch (IOException ioe) {
          LOGGER.log(Level.WARNING, "unexpected exception", ioe);
          Assertions.fail("Constructor threw IOException");
        }
        try {
          int pos = -1;
          int value = 0;
          while (pos == -1 || b1[pos] == (byte) (value)) {
            pos = sr.nextInt(b1.length);
            value = sr.nextInt(256);
          }
          byte old = b1[pos];
          b1[pos] = (byte) value;
          Assertions.assertFalse(s.verify(b1, sig, a), "Error while verifying a bad signature (returned good; old was " + old + "; new was " + value + "; pos was " + pos + ")");
        } catch (IOException ioe) {
          LOGGER.log(Level.FINE, "verification of bad signature threw an exception (this is OK)");
        }
      }
    }

  }

  @Test
  public void fuzzingAsymmetricEncryption() {
    for (Algorithm alg : Algorithm.getAlgorithms(AlgorithmType.ASYMMETRIC)) {
      for (int size : new int[]{alg.getKeySize(SecurityLevel.LOW), alg.getKeySize(SecurityLevel.MEDIUM), alg.getKeySize(SecurityLevel.HIGH), alg.getKeySize(SecurityLevel.QUANTUM)}) {
        try {
          int j = (int) Math.min(Math.pow(2, ksDisc / 8 / size), 100);
          LOGGER.log(Level.INFO, "Testing " + alg + "/" + size + " (" + j + " passes)");
          for (int i = 0; i < j; i++) {
            asymmetricEncryptionTestRun(alg, size);
          }
        } catch (Exception e) {
          LOGGER.log(Level.WARNING, "Unexpected exception", e);
          Assertions.fail("fuzzer encountered exception in Symmetric en/decryption test with algorithm " + alg);
        }
      }
    }
  }

  @Test
  public void testAsymmetricEncryptionCornerCases() {
    for (Algorithm alg : Algorithm.getAlgorithms(AlgorithmType.ASYMMETRIC)) {
      for (SecurityLevel level : SecurityLevel.values()) {
        for (Padding pad : Padding.getAlgorithms(AlgorithmType.ASYMMETRIC)) {
          for (Mode m : Mode.getModes(alg)) {
            try {
              int max = pad.getMaxSize(alg.getKeySize(level));
              LOGGER.log(Level.INFO, "Doing " + alg + "/" + pad + "/ {0," + max + "}");
              if (max > 0) {
                asymmetricEncryptionTestRun(alg, pad, m, alg.getKeySize(level), 0);
                asymmetricEncryptionTestRun(alg, pad, m, alg.getKeySize(level), max);
              } else {
                LOGGER.log(Level.INFO, "Skipped (key size too small for padding)");
              }
            } catch (Exception e) {
              LOGGER.log(Level.WARNING, "Unexpected exception", e);
              Assertions.fail("fuzzer encountered exception in Symmetric en/decryption test with algorithm " + alg.toString());
            }
          }
        }
      }
    }
  }

  @Test
  public void transferKeyTest() {
    for (Algorithm alg : Algorithm.getAlgorithms(AlgorithmType.ASYMMETRIC)) {
      int size = alg.getKeySize();
      try {
        LOGGER.log(Level.INFO, "starting tests with " + alg + " and keysize " + size);
        for (int i = 0; i < ksDisc / size; i++) {
          LOGGER.log(Level.FINE, "starting test " + (i + 1) + " of " + ksDisc / size);
          System.out.print(".");
          AlgorithmParameter p = alg.getParameters(SecurityLevel.LOW);
          AsymmetricKey k1 = new AsymmetricKey(p);
          AsymmetricKey k2 = new AsymmetricKey(p);
          k2.setPrivateKey(k1.getPrivateKey());
          k2.setPublicKey(k1.getPublicKey());
          Assertions.assertTrue(k1.equals(k2), "error in key transfer cycle with " + alg + " ");
          Assertions.assertTrue(Integer.parseInt(p.get(Parameter.KEYSIZE)) == k1.getKeySize(), "error in key size 1");
          Assertions.assertTrue(Integer.parseInt(p.get(Parameter.KEYSIZE)) == k2.getKeySize(), "error in key size 2");
          Assertions.assertTrue(Arrays.equals(k1.toBytes(DumpType.ALL_UNENCRYPTED), k2.toBytes(DumpType.ALL_UNENCRYPTED)), "reencode error in key transfer cycle with " + alg + " ");
        }
        System.out.println("");
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Unexpected exception", e);
        Assertions.fail("fuzzer encountered exception in Symmetric en/decryption test with algorithm " + alg);
      }
    }
  }

  @Test
  public void asymmetricKeySizeTest() {
    Assertions.assertTrue(Algorithm.RSA.getKeySize(SecurityLevel.LOW) == 1024, "getKeySize for RSA LOW is bad (got " + Algorithm.RSA.getKeySize(SecurityLevel.LOW) + ")");
    Assertions.assertTrue(Algorithm.RSA.getKeySize(SecurityLevel.MEDIUM) == 2048, "getKeySize for RSA MEDIUM is bad (got " + Algorithm.RSA.getKeySize(SecurityLevel.MEDIUM) + ")");
    Assertions.assertTrue(Algorithm.RSA.getKeySize(SecurityLevel.HIGH) == 4096, "getKeySize for RSA HIGH is bad (got " + Algorithm.RSA.getKeySize(SecurityLevel.HIGH) + ")");
    Assertions.assertTrue(Algorithm.RSA.getKeySize(SecurityLevel.QUANTUM) == 8192, "getKeySize for RSA QUANTUM is bad (got " + Algorithm.RSA.getKeySize(SecurityLevel.QUANTUM) + ")");
    Assertions.assertTrue(Algorithm.RSA.getKeySize(SecurityLevel.LOW) == 1024, "getKeySize for RSA LOW 2 is bad (got " + Algorithm.RSA.getKeySize(SecurityLevel.LOW) + ")");
    Assertions.assertTrue(Algorithm.EC.getKeySize(SecurityLevel.QUANTUM) == 521, "getKeySize for EC QUANTUM is bad (got " + Algorithm.EC.getKeySize(SecurityLevel.QUANTUM) + ")");
    Assertions.assertTrue(Algorithm.EC.getKeySize(SecurityLevel.LOW) == 384, "getKeySize for EC LOW is bad (got " + Algorithm.EC.getKeySize(SecurityLevel.LOW) + ")");

    Algorithm[] a = Algorithm.values();
    SecurityLevel[] sl = SecurityLevel.values();
    Map<String, Integer> lm = new HashMap<>();
    LOGGER.log(Level.INFO, "fuzzing key size test");
    int repeat = 10000;
    for (int i = 0; i < repeat; i++) {
      // pick random algrithm
      Algorithm ta = a[(int) (Math.random() * a.length)];
      SecurityLevel ts = sl[(int) (Math.random() * sl.length)];
      String s = ta.toString() + "/" + ts.toString();

      LOGGER.log(Level.INFO, "  starting test " + (i + 1) + " with " + s + " (" + i + "/" + repeat + ")");

      Integer size = lm.get(s);
      if (size == null) {
        lm.put(s, ta.getKeySize(ts));
      } else {
        Integer size2 = lm.get(s);
        Assertions.assertTrue(size2.intValue() == size.intValue(), "Failed fuzzing for " + s + " (was: " + size2.intValue() + "; is new:" + size.intValue() + ")");
      }
    }
  }

  @Test
  public void fuzzingAsymmetricKeyPadding() {
    for (Padding p : Padding.getAlgorithms(AlgorithmType.ASYMMETRIC)) {
      try {
        for (Algorithm a : Algorithm.getAlgorithms(AlgorithmType.ASYMMETRIC)) {
          LOGGER.log(Level.INFO, "testing " + a + "/" + p);
          int maximumPayload = -1;
          int size = 0;
          SecurityLevel sl = SecurityLevel.LOW;
          while (maximumPayload < 0 && maximumPayload != -100000) {
            size = a.getKeySize(sl);
            maximumPayload = p.getMaxSize(size);
            if (maximumPayload < 0 && sl == SecurityLevel.QUANTUM) {
              LOGGER.log(Level.INFO, "  skipping test for " + a + "/" + p + " due to insufficient key length");
              maximumPayload = -100000;
            } else if (maximumPayload < 0) {
              LOGGER.log(Level.INFO, "  max payload is " + maximumPayload);
              sl = sl.next();
            }
          }
          if (maximumPayload == -100000) {
            LOGGER.log(Level.INFO, "  What is going on here??");
            break;
          }
          LOGGER.log(Level.INFO, "  testing " + a + " with level " + sl + " (" + a.getKeySize(sl) + ")");
          AsymmetricKey ak = new AsymmetricKey(a.getParameters(sl));
          ak.setPadding(p);
          boolean supported = true;
          try {
            ak.encrypt(new byte[]{'B'});
          } catch (IOException ioe) {
            supported = false;
          }
          if (!supported) {
            LOGGER.log(Level.INFO, "  skipped reason=unsupported/" + a + "/" + p + "/" + ak.getMode());
          } else {
            for (int i = 0; i < 100; i++) {
              ak = new AsymmetricKey(a.getParameters(sl));
              ak.setPadding(p);
              Assertions.assertTrue(maximumPayload > 1, "negative maximum payload for " + a + "/" + size + "/" + p);
              maximumPayload = ak.getPadding().getMaxSize(size);
              byte[] b = new byte[maximumPayload];
              LOGGER.log(Level.INFO, "    Algorithm " + ak.getAlgorithm() + "[keySize=" + ak.getKeySize() + "]/" + ak.getMode() + "/" + ak.toString().toString() + "/maxPayload=" + maximumPayload);
              sr.nextBytes(b);
              try {
                byte[] b2 = ak.decrypt(ak.encrypt(b));
                Assertions.assertTrue(Arrays.equals(b, b2), "byte arrays must be equal after re-decryption (" + b.length + "!=" + b2.length + ";padding:" + ak.getPadding() + ";max_payload:" + ak.getPadding().getMaxSize(ak.getKeySize()) + ")");
              } catch (Exception e) {
                e.printStackTrace();
                Assertions.fail("Exception while using " + b.length + " bytes on " + ak);
              }
            }
          }
          LOGGER.log(Level.INFO, "  done " + a + "/" + p + " with level " + sl);

        }
      } catch (IOException ioe) {
        ioe.printStackTrace();
        Assertions.fail("got exception while fuzzing padding");
      }
    }
  }

  @Test
  public void writeAsAsn1() {
    for (Algorithm a : Algorithm.getAlgorithms(AlgorithmType.ASYMMETRIC)) {
      try {
        AsymmetricKey ak = new AsymmetricKey(a.getParameters(SecurityLevel.MEDIUM));
        File f = new File("testfile_AsymmetricKey_" + a.getAlgorithmFamily() + ".der");
        OutputStream o = new FileOutputStream(f);
        o.write(ak.toBytes(DumpType.ALL_UNENCRYPTED));
        o.close();
      } catch (Exception e) {
        Assertions.fail("unexpected exception");
      }
    }
  }
}
