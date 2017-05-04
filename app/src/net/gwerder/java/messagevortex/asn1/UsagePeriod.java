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

import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by martin.gwerder on 19.04.2016.
 */
public class UsagePeriod extends AbstractBlock {

    public static final int TAG_NOT_BEFORE =0;
    public static final int TAG_NOT_AFTER  =1;

    protected Date notBefore =null;
    protected Date notAfter  =null;

    public UsagePeriod(long seconds) {
        notBefore= new Date();
        notAfter = new Date( notBefore.getTime()+seconds*1000L);
    }

    public UsagePeriod(byte[] b) throws IOException {
        ASN1InputStream aIn=new ASN1InputStream( b );
        parse(aIn.readObject());
    }

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

    public Date getNotBefore() {
        return notBefore;
    }

    public Date getNotAfter() {
        return notAfter;
    }

    public Date setNotBefore(Date d) {
        Date d2=notBefore;
        notBefore=d;
        return d2;
    }

    public Date setNotAfter(Date d) {
        Date d2=notAfter;
        notAfter=d;
        return d2;
    }

    @Override
    public ASN1Object toASN1Object() {
        ASN1EncodableVector v = new ASN1EncodableVector();
        if(notBefore!=null) {
            v.add( new DERTaggedObject( true,TAG_NOT_BEFORE,new DERGeneralizedTime( notBefore ) ) );
        }
        if(notAfter !=null) {
            v.add( new DERTaggedObject( true,TAG_NOT_AFTER ,new DERGeneralizedTime( notAfter  ) ) );
        }
        return new DERSequence(v);
    }

    public String dumpValueNotation(String prefix) {
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

}
