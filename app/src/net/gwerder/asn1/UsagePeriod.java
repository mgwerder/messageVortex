package net.gwerder.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by martin.gwerder on 19.04.2016.
 */
public class UsagePeriod extends Block {

    protected Date notBefore =null;
    protected Date notAfter  =null;

    public UsagePeriod(long seconds) {
        notBefore= new Date();
        notAfter = new Date( notBefore.getTime()+seconds*1000);
    }

    public UsagePeriod(ASN1Encodable to) throws ParseException {
        parse(to);
    }

    protected void parse(ASN1Encodable to) throws ParseException {
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        for(ASN1Encodable e:s1.toArray()) {
            ASN1TaggedObject tag=ASN1TaggedObject.getInstance(e);
            if(tag.getTagNo()==0) notBefore = ASN1GeneralizedTime.getInstance(tag.getObject()).getDate();
            if(tag.getTagNo()==1) notAfter  = ASN1GeneralizedTime.getInstance(tag.getObject()).getDate();
        }
    }

    @Override
    public ASN1Encodable encodeDER() {
        // FIXME
        return null;
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss.SSSZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        if(notBefore!=null) sb.append(prefix+"  notBefore \""+sdf.format(notBefore)+"\""+(notAfter!=null?",":"")+CRLF);
        if(notAfter!=null)  sb.append(prefix+"  notAfter \""+sdf.format(notAfter)+"\""+CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }

}
