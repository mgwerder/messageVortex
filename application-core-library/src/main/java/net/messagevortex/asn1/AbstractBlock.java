package net.messagevortex.asn1;

import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.util.encoders.Base64;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract class collecting all ASN1 Block parser classes.
 *
 */
public abstract class AbstractBlock implements Block {

  protected static final String CRLF = "\r\n";

  /***
   * <p>Converts the values of a given String to a byte array.</p>
   * @param s the string to be converted containing hex digits
   * @return the resulting byte array
   */
  public static byte[] fromHex(String s) {
    if (s == null) {
      return null;
    }
    int len = s.length();
    byte[] data = new byte[Math.max(0,(len - 3) / 2)];
    for (int i = 1; i < len - 2; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
              + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  /***
   * <p>Decode a base64 encoded string representation to a byte array.</p>
   * @param b the string to be decoded
   * @return the decoded equivalent
   */
  public static byte[] fromBase64(String b) {
    return Base64.decode(b);
  }

  /***
   * <p>Get a base64 encoded string representation of the byte array.</p>
   * @param b the byte array to be encoded
   * @return the encoded equivalent
   */
  public static String toBase64(byte[] b) {
    return new String(Base64.encode(b), StandardCharsets.UTF_8);
  }

  /***
   * <p>Converts a byte array to a hex representation.</p>
   *
   * @param data the byte array to be converted to hex
   * @return the resuting hex string
   */
  public static String toHex(byte[] data) {
    byte[] bytes = data;
    if (bytes == null) {
      bytes = new byte[0];
    }
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02X", b));
    }
    return "'" + sb + "'H";
  }

  /***
   * <p>Convert an ASN.1 Bit String to the respective string representation.</p>
   * @param bs The BitString to be represented
   * @return the resulting string.
   */
  public static String toBitString(ASN1BitString bs) {
    if (bs == null) {
      return "''B";
    }
    int i = bs.getBytes().length * 8 - bs.getPadBits();
    if (i % 8 == 0) {
      return toHex(bs.getOctets());
    }
    StringBuilder ret = new StringBuilder();
    ret.append('\'');
    int j = 0;
    byte k = 0;
    byte[] b = bs.getBytes();
    while (i > 0) {
      int bit = ((b[j] >> (7 - k)) & 1);
      ret.append(bit > 0 ? '1' : '0');
      k++;
      if (k > 7) {
        k = 0;
        j++;
      }
      i--;
    }
    return ret.append("'B").toString();
  }

  protected void parse(byte[] b) throws IOException {
    parse(ASN1Sequence.getInstance(b));
  }

  protected abstract void parse(ASN1Encodable to) throws IOException;

  protected static byte[] toDer(ASN1Object a) {
    if (a == null) {
      throw new NullPointerException("null object may not be encoded in DER");
    }
    try {
      return a.getEncoded();
    } catch (IOException ioe) {
      // should never occur as we have no IO
      Logger.getLogger("VortexMessage").log(Level.SEVERE,"Exception while encoding object",ioe);
      return null;
    }
  }

  /***
   * <p>Dumps the object as ASN.1 der encoded byte array.</p>
   *
   * @param dumpType  the dump type to be used (@see DumpType)
   * @return          the der encoded byte array
   * @throws IOException if encoding was unsuccessful
   */
  public byte[] toBytes(DumpType dumpType) throws IOException {
    ASN1Object o = toAsn1Object(dumpType);
    if (o == null) {
      throw new IOException("Got a null reply from toAsn1Object ... get coding man");
    }
    return toDer(o);
  }


  protected String prepareDump(String s) {
    return s.replaceAll("encrypted *'[^']*'H", "encrypted '<encryptedString>'H");
  }

}
