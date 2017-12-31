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

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
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
public class UsagePeriod extends AbstractBlock  implements Serializable {

    public static final long serialVersionUID = 100000000017L;

    public static final int TAG_NOT_BEFORE =0;
    public static final int TAG_NOT_AFTER  =1;

    protected Date notBefore ;
    protected Date notAfter  ;

    /***
     * Creates a new object valid from this point in time for a duration of the specified amount of seconds.
     *
     * @param seconds The number of seconds to be valid
     */
    public UsagePeriod(long seconds) {
        notBefore= new Date();
        notAfter = new Date( notBefore.getTime()+seconds*1000L);
    }

    /***
     * Creates a new object by parsing the passed ASN.1 byte stream.
     *
     * @param b            the stream to be parsed
     * @throws IOException if parsing fails
     */
    public UsagePeriod(byte[] b) throws IOException {
        ASN1InputStream aIn=null;
        try {
            aIn = new ASN1InputStream(b);
            parse(aIn.readObject());
        } finally {
            if(aIn!=null) {
                aIn.close();
            }
        }

    }

    /***
     * Creates a new object by parsing the passed ASN.1 object.
     *
     * @param to           the stream to be parsed
     * @throws IOException if parsing fails
     */
    public UsagePeriod(ASN1Encodable to) throws IOException {
        parse(to);
    }

    protected void parse(ASN1Encodable to) throws IOException {
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        for(ASN1Encodable e:s1.toArray()) {
            ASN1TaggedObject tag=ASN1TaggedObject.getInstance(e);
            if(tag.getTagNo()==TAG_NOT_BEFORE && notBefore==null) {
                try{
                    notBefore = ASN1GeneralizedTime.getInstance(tag.getObject()).getDate();
                }catch(ParseException pe) {
                    throw new IOException( "unable to parse notAfter",pe);
                }
            } else if(tag.getTagNo()==TAG_NOT_AFTER  && notAfter==null) {
                try {
                    notAfter = ASN1GeneralizedTime.getInstance( tag.getObject() ).getDate();
                }catch(ParseException pe) {
                    throw new IOException( "unable to parse notAfter",pe);
                }
            } else {
                throw new IOException("Encountered unknown or repeated Tag number in Usage Period ("+tag.getTagNo()+")");
            }
        }
    }

    /***
     * Gets the start of validity.
     *
     * @return the currently set start of the validity
     */
    public Date getNotBefore() {
        return (Date)notBefore.clone();
    }

    /***
     * Sets the start date of validity.
     *
     * @param validityStart the new point in time to be set as start for the validity
     * @return              the previously set point in time
     */
    public Date setNotBefore(Date validityStart) {
        Date d2=notBefore;

        notBefore=(Date)validityStart.clone();
        return d2;
    }

    /***
     * Gets the date of expiry
     *
     * @return the currently set date of expiry
     */
    public Date getNotAfter() {
        return (Date)notAfter.clone();
    }

    /***
     * Sets the Date for expiriy of the validity
     *
     * @param pointInTime  the new date to be set
     * @return             the previously set date
     */
    public Date setNotAfter(Date pointInTime) {
        Date d2=notAfter;
        notAfter=(Date)pointInTime.clone();
        return d2;
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) {
        ASN1EncodableVector v = new ASN1EncodableVector();
        if(notBefore!=null) {
            v.add( new DERTaggedObject( true,TAG_NOT_BEFORE,new DERGeneralizedTime( notBefore ) ) );
        }
        if(notAfter !=null) {
            v.add( new DERTaggedObject( true,TAG_NOT_AFTER ,new DERGeneralizedTime( notAfter  ) ) );
        }
        return new DERSequence(v);
    }

    /***
     * Dumps the object as ASN.1 value notation.
     *
     * @param prefix  the prefix to be prepended in front of each line
     * @return        the string representation of the object
     */
    @Override
    public String dumpValueNotation(String prefix,DumpType dumpType) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        if(notBefore!=null) {
            sb.append(prefix+"  notBefore \""+sdf.format(notBefore)+"Z\""+(notAfter!=null?",":"")+CRLF);
        }
        if(notAfter!=null)  {
            sb.append(prefix+"  notAfter  \""+sdf.format(notAfter )+"Z\""+CRLF);
        }
        sb.append(prefix+"}");
        return sb.toString();
    }

    @Override
    public UsagePeriod clone() {
        UsagePeriod ret=new UsagePeriod(0);
        ret.notAfter=notAfter;
        ret.notBefore=notBefore;
        return ret;
    }

}
