package net.gwerder.java.messagevortex.asn1;

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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import net.gwerder.java.messagevortex.routing.operation.BitShifter;
import org.bouncycastle.asn1.ASN1Choice;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * Represents a the Blending specification of the routing block.
 */
public abstract class AbstractRedundancyOperation
                extends Operation implements ASN1Choice, Serializable {

  public static final long serialVersionUID = 100000000032L;

  public static final int INPUT_ID = 16000;
  public static final int DATA_STRIPES = 16001;
  public static final int REDUNDANCY = 16002;
  public static final int KEYS = 16003;
  public static final int OUTPUT_ID = 16004;
  public static final int GF_SIZE = 16005;

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }

  int inputId;
  int dataStripes = 1;
  int redundancyStripes = 1;
  final ArrayList<SymmetricKey> stripeKeys = new ArrayList<>();
  int outputId;
  int gfSize = 4;

  // Required for template objects
  AbstractRedundancyOperation() {
  }

  /***
   * <p>Creates an apropriate operation with the given GF size and properties.</p>
   *
   * @param inputId       first ID of the input workspace
   * @param dataStripes   number of data stripes contained in operation
   * @param redundancy    number of redundancy stripes
   * @param stripeKeys    keys for the resiulting stripes (number should be dataStripes+redundancy)
   * @param newFirstId    first output ID
   * @param gfSize        Size of the Galoise Field in bits
   */
  public AbstractRedundancyOperation(int inputId, int dataStripes, int redundancy,
                                     List<SymmetricKey> stripeKeys, int newFirstId, int gfSize) {
    this.inputId = inputId;
    setGfSize(gfSize);
    setDataStripes(dataStripes);
    setRedundancy(redundancy);
    setKeys(stripeKeys);
    this.outputId = newFirstId;
  }

  public AbstractRedundancyOperation(ASN1Encodable to) throws IOException {
    parse(to);
  }

  protected void parse(ASN1Encodable p) throws IOException {
    LOGGER.log(Level.FINER, "Executing parse()");

    int i = 0;
    ASN1Sequence s1 = ASN1Sequence.getInstance(p);

    inputId = parseIntval(ASN1TaggedObject.getInstance(s1.getObjectAt(i++)), INPUT_ID, "inputId");
    dataStripes = parseIntval(ASN1TaggedObject.getInstance(s1.getObjectAt(i++)), DATA_STRIPES,
                              "dataStripes");
    redundancyStripes = parseIntval(ASN1TaggedObject.getInstance(s1.getObjectAt(i++)), REDUNDANCY,
                                    "redundancy");

    // reading keys
    ASN1TaggedObject to = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
    if (to.getTagNo() != KEYS) {
      throw new IOException("got unknown tag id (" + to.getTagNo() + ") when expecting keys");
    }
    ASN1Sequence s = ASN1Sequence.getInstance(to.getObject());
    synchronized (stripeKeys) {
      stripeKeys.clear();
      for (ASN1Encodable o : s) {
        stripeKeys.add(new SymmetricKey(toDer(o.toASN1Primitive())));
      }
    }

    outputId = parseIntval(ASN1TaggedObject.getInstance(s1.getObjectAt(i++)), OUTPUT_ID,
                           "outputId");
    gfSize = parseIntval(ASN1TaggedObject.getInstance(s1.getObjectAt(i++)), GF_SIZE, "gfSize");

    LOGGER.log(Level.FINER, "Finished parse()");

  }

  private int parseIntval(ASN1TaggedObject obj, int id, String description) throws IOException {
    if (obj.getTagNo() != id) {
      throw new IOException("got unknown tag id (" + id + ") when expecting " + description);
    }
    return ASN1Integer.getInstance(obj.getObject()).getValue().intValue();
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    // Prepare encoding
    LOGGER.log(Level.FINER, "Executing toAsn1Object()");

    ASN1EncodableVector v = new ASN1EncodableVector();

    LOGGER.log(Level.FINER, "  adding inputId");
    v.add(new DERTaggedObject(INPUT_ID, new ASN1Integer(inputId)));

    LOGGER.log(Level.FINER, "  adding dataStripes");
    v.add(new DERTaggedObject(DATA_STRIPES, new ASN1Integer(dataStripes)));

    LOGGER.log(Level.FINER, "  adding redundancy");
    v.add(new DERTaggedObject(REDUNDANCY, new ASN1Integer(redundancyStripes)));

    ASN1EncodableVector v2 = new ASN1EncodableVector();
    for (SymmetricKey k : stripeKeys) {
      v2.add(k.toAsn1Object(dumpType));
    }
    v.add(new DERTaggedObject(KEYS, new DERSequence(v2)));

    LOGGER.log(Level.FINER, "  adding outputId");
    v.add(new DERTaggedObject(OUTPUT_ID, new ASN1Integer(outputId)));

    LOGGER.log(Level.FINER, "  adding gfSize");
    v.add(new DERTaggedObject(GF_SIZE, new ASN1Integer(gfSize)));

    ASN1Sequence seq = new DERSequence(v);
    LOGGER.log(Level.FINER, "done toAsn1Object()");
    return seq;
  }

  /***
   * <p>Dumps the ASN1 value representation of the removeRedundancy operation.</p>
   *
   * @return the representation of the object
   */
  @Override
  public String dumpValueNotation(String prefix, DumpType dumpType) {
    StringBuilder sb = new StringBuilder();
    sb.append("  {" + CRLF);
    sb.append(prefix).append("  inputId ").append(inputId).append(',').append(CRLF);
    sb.append(prefix).append("  dataStripes ").append(dataStripes).append(',').append(CRLF);
    sb.append(prefix).append("  redundancy ").append(redundancyStripes).append(',').append(CRLF);
    sb.append(prefix).append("  keys {").append(CRLF);
    int i = stripeKeys.size();
    for (SymmetricKey sk : stripeKeys) {
      i--;
      sb.append(sk.dumpValueNotation(prefix + "  ", dumpType));
      if (i > 0) {
        sb.append(',');
      }
      sb.append(CRLF);
    }
    sb.append(prefix).append("  },").append(CRLF);
    sb.append(prefix).append("  outputId ").append(outputId).append(',').append(CRLF);
    sb.append(prefix).append("  gfSize ").append(gfSize).append(CRLF);
    sb.append(prefix).append('}');
    return sb.toString();
  }

  /***
   * <p>Sets the id of the first input id of the payload.</p>
   *
   * @param id the new first input id
   * @return the previously set first input id
   */
  public final int setInputId(int id) {
    int old = this.inputId;
    this.inputId = id;
    return old;
  }

  public final int getInputId() {
    return this.inputId;
  }

  /***
   * <p>Sets the number of data stripes for this operation.</p>
   *
   * @param stripes The number of data stripes to be used for the redundancy operation
   * @return the previously set number of stripes
   * @throws ArithmeticException if all stripes together are not accomodatable in the given GF field
   */
  public final int setDataStripes(int stripes) {
    if (stripes < 1 || stripes + this.redundancyStripes > BitShifter.lshift(2, gfSize, (byte) 33)) {
      throw new ArithmeticException("too many stripes to be acomodated in given galois field");
    }
    int old = this.dataStripes;
    this.dataStripes = stripes;
    return old;
  }

  public final int getDataStripes() {
    return this.dataStripes;
  }

  /***
   * <p>sets the number of redundancy stripes.</p>
   *
   * @param stripes the number of redundancy stripes to be set
   * @return the previous number of redundancy stripes
   * @throws ArithmeticException if the defined GF size is unable to accomodate all values
   */
  public final int setRedundancy(int stripes) {
    if (stripes < 1) {
      throw new ArithmeticException("too few stripes to be acomodated in current galois field");
    } else if (stripes + this.dataStripes > BitShifter.lshift(2, gfSize, (byte) 33)) {
      throw new ArithmeticException("too many stripes to be acomodated in current galois field");
    }
    int old = this.redundancyStripes;
    this.redundancyStripes = stripes;
    return old;
  }

  public int getRedundancy() {
    return this.redundancyStripes;
  }

  /***
   * <p>Sets the keys to be used to encrypt all input respective output fields.</p>
   *
   * @param keys a list of teys
   * @return the old list of keys
   * @throws ArithmeticException if the number of keys doees not match the number of stripes
   */
  public final SymmetricKey[] setKeys(List<SymmetricKey> keys) {
    if (this.dataStripes + this.redundancyStripes != keys.size()) {
      throw new ArithmeticException("illegal number of keys");
    }

    SymmetricKey[] old = new SymmetricKey[0];
    old = stripeKeys.toArray(new SymmetricKey[stripeKeys.size()]);
    synchronized (stripeKeys) {
      stripeKeys.clear();
      stripeKeys.addAll(keys);
    }
    return old;
  }

  /***
   * <p>Gets the omega parameter of the Galoise field.</p>
   *
   * @return the omega parameter of the GF.
   */
  public SymmetricKey[] getkeys() {
    return stripeKeys.toArray(new SymmetricKey[stripeKeys.size()]);
  }

  /***
   * <p>Sets the omega parameter of the Galoise field.</p>
   *
   * @param omega the omega of the new GF
   * @return the previous omega parameter of the GF.
   * @throws ArithmeticException if the number of all stripes in total (
   *                             data and redundancy) exceeds the address space of the GF
   */
  public final int setGfSize(int omega) {
    if (omega < 2 || omega > 16
        || this.redundancyStripes + this.dataStripes > BitShifter.lshift(2, omega, (byte) 33)) {
      throw new ArithmeticException("galois field too small for the stripes to be acomodated");
    }
    int old = this.gfSize;
    this.gfSize = omega;
    return old;
  }

  public final int getGfSize() {
    return this.gfSize;
  }

  /***
   * <p>Sets the id of the first output block of the function.</p>
   *
   * @param id the first id to ber ised
   * @return old first value (before the write
   */
  public int setOutputId(int id) {
    int old = this.outputId;
    this.outputId = id;
    return old;
  }

  /***
   * <p>gets the id of the first output payload block.</p>
   *
   * @return id of the respective block
   */
  public int getOutputId() {
    return this.outputId;
  }

}
