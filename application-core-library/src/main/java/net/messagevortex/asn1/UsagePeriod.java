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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Represents a usage period.
 */
public class UsagePeriod extends AbstractBlock implements Serializable, Comparable<UsagePeriod> {

  public static final long serialVersionUID = 100000000017L;

  public static final int TAG_NOT_BEFORE = 0;
  public static final int TAG_NOT_AFTER = 1;

  protected long notBefore = -1;
  protected long notAfter = -1;

  protected Date reference = new Date();

  protected UsagePeriodType type;

  /***
   * <p>Creates a new object valid from this point in time for a duration of the specified amount
   * of seconds.</p>
   *
   * <p>The validity time is created as relative time to the objects creation.</p>
   *
   * @param seconds The number of seconds to be valid
   */
  public UsagePeriod(long seconds) {
    this(0, seconds);
  }

  /***
   * <p>Creates a new object valid from this point plus startSeconds in time for a duration
   * of the specified amount of seconds.</p>
   *
   * <p>The validity time is created as relative time to the objects creation.</p>
   *
   * @param startSeconds the number of seconds after the current time the duration starts
   * @param durationSeconds the number of seconds of the duration
   */
  public UsagePeriod(long startSeconds, long durationSeconds) {
    this(startSeconds, durationSeconds, new Date());
  }

  /***
   * <p>Constructor to create a relative usage period.</p>
   *
   * @param startSeconds     the number of seconds after the reference to start the period
   * @param durationSeconds  the number of seconds the duration lasts
   * @param reference        the date reference
   */
  public UsagePeriod(long startSeconds, long durationSeconds, Date reference) {
    notBefore = reference.getTime() + startSeconds * 1000L;
    notAfter = notBefore + durationSeconds * 1000L;
    this.reference = new Date(reference.getTime());
    type = UsagePeriodType.RELATIVE;
  }

  /***
   * <p>Creates a new object valid from this point in time for the maximum possible duration.</p>
   */
  public UsagePeriod() {
    this(Long.MAX_VALUE);
  }

  /***
   * <p>Copy constructor to copy a usage period.</p>
   *
   * @param p the usage period to be copied
   */
  public UsagePeriod(UsagePeriod p) {
    notAfter = p.notAfter;
    notBefore = p.notBefore;
    reference = p.reference;
    type = p.type;
  }

  /***
   * <p>Creates a new object valid from this point in time for a duration of the specified amount of
   * seconds.</p>
   *
   * <p>The validity time is created as absolute time.</p>
   *
   * @param from  the moment the object gains validity
   * @param to    the moment the object validity ends
   */
  public UsagePeriod(Date from, Date to) {
    notBefore = from.getTime();
    notAfter = to.getTime();
    type = UsagePeriodType.ABSOLUTE;
  }

  /***
   * <p>Creates a new object by parsing the passed ASN.1 byte stream.</p>
   *
   * @param b            the stream to be parsed
   * @throws IOException if parsing fails
   */
  public UsagePeriod(byte[] b) throws IOException {
    try (ASN1InputStream aIn = new ASN1InputStream(b)) {
      parse(aIn.readObject());
    }
  }

  /***
   * <p>Creates a new object by parsing the passed ASN.1 object.</p>
   *
   * @param to           the stream to be parsed
   * @throws IOException if parsing fails
   */
  public UsagePeriod(ASN1Encodable to) throws IOException {
    parse(to);
  }

  protected final void parse(ASN1Encodable to) throws IOException {
    ASN1TaggedObject s1 = ASN1TaggedObject.getInstance(to);
    ASN1Sequence s2 = ASN1Sequence.getInstance(s1.getObject());
    if (s1.getTagNo() == UsagePeriodType.ABSOLUTE.getId()) {
      // get absolute
      type = UsagePeriodType.ABSOLUTE;
      for (ASN1Encodable e : s2.toArray()) {
        ASN1TaggedObject tag = ASN1TaggedObject.getInstance(e);
        if (tag.getTagNo() == TAG_NOT_BEFORE && notBefore == -1) {
          try {
            notBefore = ASN1GeneralizedTime.getInstance(tag.getObject()).getDate().getTime();
          } catch (ParseException pe) {
            throw new IOException("unable to parse notAfter", pe);
          }
        } else if (tag.getTagNo() == TAG_NOT_AFTER && notAfter == -1) {
          try {
            notAfter = ASN1GeneralizedTime.getInstance(tag.getObject()).getDate().getTime();
          } catch (ParseException pe) {
            throw new IOException("unable to parse notAfter", pe);
          }
        } else {
          throw new IOException("Encountered unknown or repeated Tag number in Usage Period ("
                  + tag.getTagNo() + ")");
        }
      }
    } else if (s1.getTagNo() == UsagePeriodType.RELATIVE.getId()) {
      type = UsagePeriodType.RELATIVE;
      reference = new Date();
      for (ASN1Encodable e : s2.toArray()) {
        ASN1TaggedObject tag = ASN1TaggedObject.getInstance(e);
        if (tag.getTagNo() == TAG_NOT_BEFORE && notBefore == -1) {
          notBefore = ASN1Integer.getInstance(tag.getObject()).getValue().longValue() * 1000L
                  + reference.getTime();
        } else if (tag.getTagNo() == TAG_NOT_AFTER && notAfter == -1) {
          notAfter = ASN1Integer.getInstance(tag.getObject()).getValue().longValue() * 1000L
                  + reference.getTime();
        } else {
          throw new IOException("Encountered unknown or repeated Tag number in Usage Period ("
                  + tag.getTagNo() + ")");
        }
      }
    }
  }

  /***
   * <p>Gets the start of validity.</p>
   *
   * @return the currently set start of the validity
   */
  public Date getNotBefore() {
    return new Date(notBefore / 1000L);
  }

  /***
   * <p>Sets the start date of validity.</p>
   *
   * @param validityStart the new point in time to be set as start for the validity
   * @return the previously set point in time
   */
  public Date setNotBefore(Date validityStart) {
    Date d2 = new Date(notBefore / 1000L);
    notBefore = validityStart.getTime();
    type = UsagePeriodType.ABSOLUTE;
    return d2;
  }

  /***
   * <p>Gets the date of expiry.</p>
   *
   * @return the currently set date of expiry
   */
  public Date getNotAfter() {
    return new Date(notAfter / 1000L);
  }

  /***
   * <p>Sets the Date for expiriy of the validity.</p>
   *
   * @param pointInTime  the new date to be set
   * @return the previously set date
   */
  public Date setNotAfter(Date pointInTime) {
    Date d2 = new Date(notAfter / 1000L);
    notAfter = pointInTime.getTime();
    type = UsagePeriodType.ABSOLUTE;
    return d2;
  }

  /***
   * <p>Gets the the absolute epoch of the start time.</p>
   *
   * @return the absolute epoch in seconds
   */
  public long getBeforeInt() {
    return (reference == null ? notBefore / 1000L : (notBefore - reference.getTime()) / 1000L);
  }

  /***
   * <p>Gets the the absolute epoch of the end time.</p>
   *
   * @return the absolute epoch in seconds
   */
  public long getAfterInt() {
    return (reference == null ? notAfter / 1000L : (notAfter - reference.getTime()) / 1000L);
  }

  @Override
  public ASN1Object toAsn1Object(DumpType dumpType) {
    ASN1EncodableVector v = new ASN1EncodableVector();
    if (type == UsagePeriodType.ABSOLUTE) {
      if (notBefore != -1) {
        v.add(new DERTaggedObject(true, TAG_NOT_BEFORE,
                new DERGeneralizedTime(new Date(notBefore))));
      }
      if (notAfter != -1) {
        v.add(new DERTaggedObject(true, TAG_NOT_AFTER,
                new DERGeneralizedTime(new Date(notAfter))));
      }
    } else {
      if (notBefore != -1) {
        v.add(new DERTaggedObject(true, TAG_NOT_BEFORE,
                new ASN1Integer((notBefore - reference.getTime()) / 1000L)));
      }
      if (notAfter != -1) {
        v.add(new DERTaggedObject(true, TAG_NOT_AFTER,
                new ASN1Integer((notAfter - reference.getTime()) / 1000L)));
      }
    }
    return new DERTaggedObject(type.getId(), new DERSequence(v));
  }

  /***
   * <p>Dumps the object as ASN.1 value notation.</p>
   *
   * @param prefix  the prefix to be prepended in front of each line
   * @return the string representation of the object
   */
  @Override
  public String dumpValueNotation(String prefix, DumpType dumpType) {
    StringBuilder sb = new StringBuilder();
    sb.append("{" + CRLF);
    if (type == UsagePeriodType.ABSOLUTE) {
      sb.append(prefix).append("  absolute [");
    } else if (type == UsagePeriodType.RELATIVE) {
      sb.append(prefix).append("  relative [");
    } else {
      sb.append(prefix).append("  /* UNKNOWN: ").append(type).append(" */").append(CRLF);
    }
    sb.append(type.getId()).append("] {").append(CRLF);
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    if (notBefore != -1) {
      sb.append(prefix).append("    notBefore ");
      if (type == UsagePeriodType.ABSOLUTE) {
        sb.append('"').append(sdf.format(notBefore)).append("Z\"");
      } else {
        sb.append((notBefore - reference.getTime()) / 1000L);
      }
      sb.append((notAfter != -1 ? ',' : "")).append(CRLF);
    }
    if (notAfter != -1) {
      sb.append(prefix).append("    notAfter  ");
      if (type == UsagePeriodType.ABSOLUTE) {
        sb.append('"').append(sdf.format(notAfter)).append("Z\"");
      } else {
        sb.append((notAfter - reference.getTime()) / 1000L);
      }
      sb.append(CRLF);
    }
    sb.append(prefix).append("  ").append('}').append(CRLF);
    sb.append(prefix).append('}');
    return sb.toString();
  }

  public boolean inUsagePeriod() {
    return inUsagePeriod(reference);
  }

  /***
   * <p>checks whether the reference time is within the specified usage time.</p>
   *
   * @param reference the time to to be taken into account when checking
   * @return true if within bounds
   */
  public boolean inUsagePeriod(Date reference) {
    long now = new Date().getTime();
    return now >= notBefore && now <= notAfter;
  }

  @Override
  public int compareTo(UsagePeriod other) {
    if (getBeforeInt() > other.getBeforeInt()) {
      return 1;
    } else if (getBeforeInt() == other.getBeforeInt()) {
      if (getAfterInt() == other.getAfterInt()) {
        if (type == UsagePeriodType.ABSOLUTE && other.type != UsagePeriodType.ABSOLUTE) {
          return 1;
        } else if (type == UsagePeriodType.RELATIVE && other.type != UsagePeriodType.RELATIVE) {
          return -1;
        } else {
          return 0;
        }
      } else if (getAfterInt() > other.getAfterInt()) {
        return 1;
      } else {
        return -1;
      }
    } else {
      return -1;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof UsagePeriod) {
      return compareTo((UsagePeriod) o) == 0;
    } else {
      return false;
    }
  }
  
  @Override
  public int hashCode() {
    return dumpValueNotation("",DumpType.ALL).hashCode();
  }

}
