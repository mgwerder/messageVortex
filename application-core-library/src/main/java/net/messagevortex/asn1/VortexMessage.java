package net.messagevortex.asn1;

// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/***
 * <p>The main class representing the main message object.</p>
 *
 * <p>This part is specified as vortexMessage in the file asn.1/messageBlocks.asn1</p>
 */
public class VortexMessage extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 100000000018L;

  public static final int PREFIX_PLAIN = 10011;
  public static final int PREFIX_ENCRYPTED = 10012;
  public static final int INNER_MESSAGE_PLAIN = 10021;
  public static final int INNER_MESSAGE_ENCRYPTED = 10022;

  private PrefixBlock prefix;
  private InnerMessageBlock innerMessage;
  private AsymmetricKey decryptionKey;

  private static final Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Exception()).getStackTrace()[0].getClassName());
  }

  /***
   * <p>Creates an empty message with a new symmetric encrypption decryptionKey in the prefix.</p>
   *
   * @throws IOException if the constructor was unable to build a message skeleton from the
   *                     default values
   */
  private VortexMessage() throws IOException {
    prefix = new PrefixBlock();
    innerMessage = new InnerMessageBlock();
    decryptionKey = null;
  }

  /***
   * <p>Parses a byte array to a  VortexMessage.</p>
   *
   * @param is  the input stream to be parsed
   * @param dk the decryptionKey required to decrypt the prefix
   *
   * @throws IOException if there was a problem parsing or decrypting the object
   */
  public VortexMessage(InputStream is, AsymmetricKey dk) throws IOException {
    // set a decryption key (is any)
    setDecryptionKey(dk);

    // Allocate a byte array output stream and an apropriate buffer
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    byte[] chunk = new byte[1024 * 1024];

    // read buffer chunks into output stream until all is read
    int i = is.read(chunk, 0, chunk.length - 1);
    while (i > 0) {
      buffer.write(chunk, 0, i);
      i = is.read(chunk, 0, chunk.length - 1);
    }

    // convert and parse resulting byte array
    try {
      parse(buffer.toByteArray());
    } catch (EOFException | IllegalArgumentException eex) {
      throw new IOException("Exception while parsing byte array", eex);
    }

    // tear down output buffer
    buffer.close();
  }

  /***
   * <p>Parses a byte array to a  VortexMessage.</p>
   *
   * @param b  the byte array to be parsed
   * @param dk the decryptionKey required to decrypt the prefix
   *
   * @throws IOException if there was a problem parsing or decrypting the object
   */
  public VortexMessage(byte[] b, AsymmetricKey dk) throws IOException {
    setDecryptionKey(dk);
    parse(b);
  }

  /***
   * <p>Creates a new message block.</p>
   *
   * @param pre the prefix block to use
   * @param im  the inner message block to use
   * @throws IOException if there was an error generating the kay
   */
  public VortexMessage(PrefixBlock pre, InnerMessageBlock im) throws IOException {
    this();

    // check for existing prefix block
    if (pre == null) {
      // create prefix block with new key if missing
      setPrefix(new PrefixBlock(new SymmetricKey()));
    } else {

      // set prefix block and extract key
      setPrefix(pre);
    }

    // store inner Message Block
    setInnerMessage(im);
  }

  /***
   * <p>gets the embedded inner message block.</p>
   *
   * @return the message block
   */
  public final InnerMessageBlock getInnerMessage() {
    return innerMessage;
  }

  /***
   * <p>Setter for the inner message block.</p>
   *
   * @param im the new inner message block
   * @return the previously set inner message block
   */
  public final InnerMessageBlock setInnerMessage(InnerMessageBlock im) {
    if (im == null) {
      throw new NullPointerException("InnerMessage may not be null");
    }
    InnerMessageBlock old = getInnerMessage();
    innerMessage = im;
    return old;
  }

  /**
   * <p>Gets the embedded prefix block.</p>
   *
   * @return the prefix block
   */
  public final PrefixBlock getPrefix() {
    return prefix;
  }

  /***
   * <p>Sets the embedded prefix block.</p>
   *
   * @param pre the new prefix block
   * @return the prefix block which was set prior to the operation
   */
  public final PrefixBlock setPrefix(PrefixBlock pre) {
    if (pre == null) {
      throw new NullPointerException("Prefix may not be null");
    }
    PrefixBlock old = getPrefix();
    prefix = pre;
    return old;
  }

  /***
   * <p>Set the currently set encryption/decryption decryptionKey (asymmetric).</p>
   *
   * @return the decryptionKey or null if not set
   */
  public final AsymmetricKey getDecryptionKey() {
    return decryptionKey;
  }

  /***
   * <p>Set the encryption/decryption decryptionKey.</p>
   *
   * @param dk sets the decryption key
   * @return the decryptionKey which has been set previously or null if the decryptionKey ha not
   *         been set
   *
   * @throws IOException if setting fails
   */
  public final AsymmetricKey setDecryptionKey(AsymmetricKey dk) throws IOException {
    AsymmetricKey old = this.decryptionKey;
    this.decryptionKey = dk != null ? new AsymmetricKey(dk) : null;
    if (prefix != null) {
      AsymmetricKey newOld = prefix.setDecryptionKey(dk);
      if (newOld != null) {
        old = newOld;
      }
    }
    if (innerMessage != null && innerMessage.getPrefix() != null) {
      AsymmetricKey newOld = innerMessage.getPrefix().setDecryptionKey(dk);
      if (newOld != null) {
        old = newOld;
      }
    }
    return old;
  }

  protected final void parse(byte[] p) throws IOException {
    try (ASN1InputStream asnIn = new ASN1InputStream(p)) {
      parse(asnIn.readObject());
    }
  }

  protected void parse(InputStream is) throws IOException {
    try (ASN1InputStream asnIn = new ASN1InputStream(is)) {
      parse(asnIn.readObject());
    }
  }

  protected void parse(ASN1Encodable p) throws IOException {
    if (p == null) {
      throw new NullPointerException("Encodable may not be null");
    }
    LOGGER.log(Level.FINER, "Executing parse()");
    int i = 0;
    ASN1Sequence s1 = ASN1Sequence.getInstance(p);

    // reading prefix
    ASN1TaggedObject to = ASN1TaggedObject.getInstance(s1.getObjectAt(i));
    i++;
    switch (to.getTagNo()) {
      case PREFIX_PLAIN:
        LOGGER.log(Level.INFO, "parsing unencrypted/plain prefix block");
        prefix = new PrefixBlock(to.getObject(), null);
        break;
      case PREFIX_ENCRYPTED:
        LOGGER.log(Level.INFO, "parsing encrypted prefix block");
        prefix = new PrefixBlock(ASN1OctetString.getInstance(to.getObject()).getOctets(),
                getDecryptionKey());
        break;
      default:
        throw new IOException("got unexpected tag number when reading prefix (" + to.getTagNo()
                + ")");
    }
    if (prefix == null) {
      throw new IOException("unable to parse prefix block");
    }

    // reading inner message
    to = ASN1TaggedObject.getInstance(s1.getObjectAt(i));
    i++;
    switch (to.getTagNo()) {
      case INNER_MESSAGE_PLAIN:
        innerMessage = new InnerMessageBlock(toDer(to.getObject()), null);
        break;
      case INNER_MESSAGE_ENCRYPTED:
        if (prefix.getKey() == null) {
          throw new IOException("unable to get key from prefix block");
        }
        byte[] stream = null;
        try {
          byte[] derStream = ASN1OctetString.getInstance(to.getObject()).getOctets();
          stream = prefix.getKey().decrypt(derStream);
        } catch (IllegalArgumentException iae) {
          throw new IOException("unable to get decrypted byte stream (1)", iae);
        }
        if (stream == null || stream.length == 0) {
          throw new IOException("unable to get decrypted byte stream "
                + "(2; stream is null or sized 0 bytes)");
        }

        innerMessage = new InnerMessageBlock(stream, getDecryptionKey());
        break;
      default:
        throw new IOException("got unexpected tag number when reading inner message ("
                + to.getTagNo() + ")");
    }

    // propagate decryption key to sub elements
    setDecryptionKey(getDecryptionKey());
  }

  /***
   * <p>Dumps the object a ASN1Object.</p>
   *
   * <p>This method is mainly useful for diagnostic purposes.</p>
   *
   * @param dt           the dumpType to apply
   * @return the requested object as ASN1Object
   * @throws IOException if any object or subobject can not be dumped
   */
  public ASN1Object toAsn1Object(DumpType dt) throws IOException {
    // Prepare encoding
    LOGGER.log(Level.FINER, "Executing toAsn1Object()");

    ASN1EncodableVector v = new ASN1EncodableVector();

    // add prefix to structure
    addPrefixBlockToAsn1(v, dt);
    // add inner message to structure
    addInnerMessageBlockToAsn1(v, dt);

    ASN1Sequence seq = new DERSequence(v);
    LOGGER.log(Level.FINER, "done toAsn1Object()");
    return seq;
  }

  private void addPrefixBlockToAsn1(ASN1EncodableVector v, DumpType dumpType) throws IOException {
    ASN1Object o = getPrefix().toAsn1Object(dumpType);
    if (o == null) {
      throw new IOException("returned prefix object may not be null");
    }
    if (dumpType == DumpType.ALL_UNENCRYPTED || getPrefix().getDecryptionKey() == null) {
      LOGGER.log(Level.INFO, "Adding unencrypted prefix block to message");
      v.add(new DERTaggedObject(PREFIX_PLAIN, o));
    } else {
      LOGGER.log(Level.INFO, "Adding encrypted prefix block to message");
      v.add(new DERTaggedObject(PREFIX_ENCRYPTED, new DEROctetString(getPrefix().toEncBytes())));
    }
  }

  private void addInnerMessageBlockToAsn1(ASN1EncodableVector v, DumpType dt) throws IOException {
    if (prefix.getKey() == null || DumpType.ALL_UNENCRYPTED == dt) {
      LOGGER.log(Level.INFO, "Adding unencrypted inner message block to message");
      v.add(new DERTaggedObject(INNER_MESSAGE_PLAIN, getInnerMessage().toAsn1Object(dt)));
    } else {
      LOGGER.log(Level.INFO, "Adding encrypted inner message block to message");
      byte[] b = toDer(getInnerMessage().toAsn1Object(dt));
      v.add(new DERTaggedObject(INNER_MESSAGE_ENCRYPTED,
              new DEROctetString(prefix.getKey().encrypt(b))));
    }
  }

  /***
   * <p>Dumps an ASN.1 value notation of a vortexMessage (PUBLIC_ONLY dump type).</p>
   *
   * @param prefix       the line prefix to be used (typically &quot;&quot;)
   * @return an ASN.1  representation of the vortexMessage
   * @throws IOException if message i not encodable due to an incomplete/invalid object state
   */
  public String dumpValueNotation(String prefix) throws IOException {
    return dumpValueNotation(prefix, DumpType.PUBLIC_ONLY);
  }

  /***
   * <p>Dumps a ASN.1 value notation of a vortexMessage.</p>
   *
   * @param prefix       the line prefix to be used (typically &quot;&quot;)
   * @param dt           specifies the type of dump.
   * @return an ASN.1  representation of the vortexMessage
   * @throws IOException if message i not encodable due to an incomplete/invalid object state
   */
  public String dumpValueNotation(String prefix, DumpType dt) throws IOException {
    StringBuilder ret = new StringBuilder();
    ret.append(prefix).append("m VortexMessage ::= {").append(CRLF);
    ret.append(prefix).append("  -- Dumping prefix").append(CRLF);
    ret.append(prefix).append("  prefix ").append(getPrefix().dumpValueNotation(prefix + "  ", dt))
            .append(',').append(CRLF);
    ret.append(prefix).append("  -- Dumping innerMessage").append(CRLF);
    ret.append(prefix).append("  innerMessage ").append(getInnerMessage()
            .dumpValueNotation(prefix + "  ", dt)).append(',').append(CRLF);
    ret.append(prefix).append('}').append(CRLF);
    return ret.toString();
  }

  /***
   * <p>Build the binary represenattion for a vortexMessage.</p>
   *
   * <p>This method returns the binary (length prefixed) representation of a vortex message.</p>
   *
   * @param dt specifies the type of dump. for sending use PUBLIC_ONLY
   * @return the binary representation of the vortexMessage
   *
   * @throws IOException if dumping fails
   */
  public byte[] toBinary(DumpType dt) throws IOException {
    return toAsn1Object(dt).getEncoded();
  }

  /***
   * <p>converts an unsigned long value into a byte array representation (LSB).</p>
   *
   * @param i the long value to be converted
   * @param num the number of bytes to be returned
   * @return the unsigned byte array representation
   */
  public static byte[] getLongAsBytes(long i, int num) {
    byte[] ret = new byte[num];
    for (int j = 0; j < num; j++) {
      ret[j] = (byte) ((i >> (j * 8)) & 0xFF);
    }
    return ret;
  }

  /***
   * <p>Converts an unsigned long value into a 32 bit byte array representation (LSB).</p>
   *
   * @param i the long value to be converted
   * @return the unsigned byte array of length 4 representation
   */
  public static byte[] getLongAsBytes(long i) {
    return getLongAsBytes(i, 4);
  }

  /***
   * <p>Converts a number of bytes into a long representation (LSB).</p>
   *
   * @param b the byte array to be converted to long
   * @return the long representation of the byte array
   */
  public static long getBytesAsLong(byte[] b) {
    if (b == null || b.length < 1 || b.length > 8) {
      throw new IllegalArgumentException("byte array must contain exactly four bytes");
    }
    long ret = 0;
    for (int i = 0; i < b.length; i++) {
      ret |= ((long) (b[i] & 0xFF)) << (i * 8);
    }
    return ret;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o.getClass() != this.getClass()) {
      return false;
    }
    VortexMessage vm = (VortexMessage) o;
    try {
      return Arrays.equals(vm.toBytes(DumpType.ALL_UNENCRYPTED),
              this.toBytes(DumpType.ALL_UNENCRYPTED));
    } catch (IOException ioe) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    try {
      return prepareDump(dumpValueNotation("", DumpType.ALL_UNENCRYPTED)).hashCode();
    } catch (IOException ioe) {
      return -1;
    }
  }
}
