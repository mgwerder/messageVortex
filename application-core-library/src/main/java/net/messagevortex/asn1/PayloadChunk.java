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

import net.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

public class PayloadChunk extends AbstractBlock implements Serializable {

  public static final long serialVersionUID = 100000000013L;

  private enum PayloadType {
    /* ASN1 tag number for a contained payload */
    PAYLOAD(100),
    /* ASN1 tag number for a contained reply block */
    REPLY(101);

    private final int id;

    PayloadType(int id) {
      this.id = id;
    }

    public int getId() {
      return id;
    }

  }

  /* the minimum required id in order to allow dumping to der */
  public static final int MIN_VALID_ID = 100;

  int id = 0;
  byte[] payload = null;
  PayloadType payloadType = PayloadType.PAYLOAD;
  UsagePeriod period;

  /***
   * <p>Creates an empty payload block.</p>
   */
  public PayloadChunk() {
    id = 0;
    payload = new byte[0];
    payloadType = PayloadType.PAYLOAD;
    period = new UsagePeriod(24L * 3600);
  }

  /***
   * <p>Creates a payload block from a ASN1 stream.</p>
   *
   * @param to the ASN.1 object of the PayloadCunk to be parsed
   * @param period the validity period to be associated with
   *
   * @throws IOException if parsing fails
   */
  public PayloadChunk(ASN1Encodable to, UsagePeriod period) throws IOException {
    parse(to);
    this.period = period;
  }

  /***
   * <p>Creates a payload block from raw data.</p>
   *
   * @param id the payload location
   * @param payload the payload content
   * @param period the validity period to be associated with
   */
  public PayloadChunk(int id, byte[] payload, UsagePeriod period) {
    setId(id);
    setPayload(payload);
    this.period = period;
  }

  /***
   * <p>Creates a der encoded ASN1 representation of the payload chunk.</p>
   *
   * @param  dumpType    the dump type to be used
   * @return the ASN.1 object
   * @throws IOException if id is too low or the payload has not been set
   */
  public ASN1Object toAsn1Object(DumpType dumpType) throws IOException {
    ASN1EncodableVector v = new ASN1EncodableVector();
    if (id < MIN_VALID_ID) {
      throw new IOException("illegal dump id is set");
    }
    v.add(new ASN1Integer(id));

    if (payloadType == PayloadType.PAYLOAD) {
      v.add(new DERTaggedObject(true, PayloadType.PAYLOAD.getId(), new DEROctetString(payload)));
    } else if (payloadType == PayloadType.REPLY) {
      v.add(new DERTaggedObject(true, PayloadType.REPLY.getId(), new DEROctetString(payload)));
    } else {
      throw new IOException("unable to dump payload block as payload and reply are empty");
    }
    return new DERSequence(v);
  }

  /***
   * <p>Set a byte array as payload.</p>
   *
   * @param b the payload to be set
   * @return the previously set payload (may have been a reply block)
   */
  public final byte[] setPayload(byte[] b) {
    byte[] opl = payload;
    if (b != null) {
      payload = Arrays.copyOf(b, b.length);
    } else {
      payload = null;
    }
    payloadType = PayloadType.PAYLOAD;
    return opl;
  }

  /***
   * <p>Gets the the currently set payload.</p>
   *
   * @return the payload as byte array or null if a replyblock has been set
   */
  public final byte[] getPayload() {
    if (payloadType != PayloadType.PAYLOAD) {
      return null;
    }
    if (payload == null) {
      return null;
    } else {
      return payload.clone();
    }
  }

  /***
   * <p>Set a byte array as reply block.</p>
   *
   * @param reply the reply block to be set
   * @return the previously set reply block (may have been a payload block)
   */
  public final byte[] setReplyBlock(byte[] reply) {
    byte[] opl = payload;
    payload = Arrays.copyOf(reply, reply.length);
    payloadType = PayloadType.REPLY;
    return opl;
  }

  /***
   * <p>Gets the the currently set reply block.</p>
   *
   * @return the reply block as byte array or null if a payload block has been set
   */
  public final byte[] getReplyBlock() {
    if (payloadType != PayloadType.REPLY) {
      return null;
    }
    return payload.clone();
  }

  public final UsagePeriod getUsagePeriod() {
    return period;
  }

  /***
   * <p>Sets the usage period of the payload cunk.</p>
   *
   * @param period the new usage period
   * @return the previously set usage period
   */
  public final UsagePeriod setUsagePeriod(UsagePeriod period) {
    UsagePeriod ret = this.period;
    this.period = period;
    return ret;
  }

  @Override
  protected final void parse(ASN1Encodable to) throws IOException {
    ASN1Sequence s1 = ASN1Sequence.getInstance(to);
    int i = 0;
    id = ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();

    ASN1TaggedObject dto = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
    if (dto.getTagNo() == PayloadType.PAYLOAD.getId()) {
      setPayload(ASN1OctetString.getInstance(dto.getObject()).getOctets());
    } else if (dto.getTagNo() == PayloadType.REPLY.getId()) {
      setReplyBlock(ASN1OctetString.getInstance(dto.getObject()).getOctets());
    } else {
      throw new IOException("got bad tag number (expected:" + PayloadType.REPLY.getId()
          + " or " + PayloadType.PAYLOAD.getId() + ";got:" + dto.getTagNo() + ")");
    }
  }

  /***
   * <p>Sets the id of the payload chunk.</p>
   *
   * @param id the id to be set
   * @return the previously set id
   */
  public final int setId(int id) {
    int ret = this.id;
    this.id = id;
    return ret;
  }

  /***
   * <p>Gets the id of the payload chunk.</p>
   *
   * @return the id currently set
   */
  public final int getId() {
    return this.id;
  }

  /***
   * <p>Dumps the current object as a value representation.</p>
   *
   * @param prefix       the prefix to be used (normally used for indentation)
   * @param dumpType     the dump type to be used (@see DumpType)
   * @return the string representation of the ASN1 object
   * @throws IOException if the payload id is below MIN_VALID_ID or no payload/reply block
   *                     has been set
   */
  @Override
  public String dumpValueNotation(String prefix, DumpType dumpType) throws IOException {
    StringBuilder sb = new StringBuilder();
    sb.append(" {").append(CRLF);
    sb.append(prefix).append("  id ").append(id).append(',').append(CRLF);
    sb.append(prefix).append("  content ");
    if (payloadType == PayloadType.PAYLOAD) {
      sb.append("payload ").append(toHex(payload)).append(CRLF);
    } else if (payloadType == PayloadType.REPLY) {
      sb.append("reply ").append(toHex(payload)).append(CRLF);
    } else {
      throw new IOException("unable to determine payload type (expected:"
          + PayloadType.REPLY.getId() + " or " + PayloadType.PAYLOAD.getId() + ";got:"
          + payloadType + ")");
    }
    sb.append(prefix).append('}');
    return sb.toString();
  }

  public boolean isInUsagePeriod() {
    return isInUsagePeriod(new Date());
  }

  /***
   * <p>Checks if the usage period passed is fully embraced in the usage period.</p>
   *
   * @param reference the usage period to be embraced
   * @return tre if embraced or no usage restriction
   */
  public boolean isInUsagePeriod(Date reference) {
    if (period == null) {
      return true;
    }
    return period.inUsagePeriod(reference);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }

    if (o.getClass() != this.getClass()) {
      return false;
    }
    PayloadChunk pl = (PayloadChunk) o;

    try {
      return dumpValueNotation("", DumpType.ALL_UNENCRYPTED)
          .equals(pl.dumpValueNotation("", DumpType.ALL_UNENCRYPTED));
    } catch (IOException ioe) {
      return false;
    }
  }

  @Override
  public int hashCode() {
    try {
      return dumpValueNotation("", DumpType.ALL_UNENCRYPTED).hashCode();
    } catch (IOException ioe) {
      return 0;
    }
  }

}
