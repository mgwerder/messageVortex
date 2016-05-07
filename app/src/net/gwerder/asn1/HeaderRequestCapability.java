package net.gwerder.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;

import java.text.ParseException;

/**
 * Created by martin.gwerder on 25.04.2016.
 */
public class HeaderRequestCapability extends HeaderRequest {

    protected UsagePeriod period = null;

    protected HeaderRequestCapability() {super();}

    public HeaderRequestCapability(ASN1Encodable ae) throws ParseException {
        parse(ae);
    }

    protected void parse(ASN1Encodable ae) throws ParseException{
        ASN1Sequence s1 = ASN1Sequence.getInstance(ae);
        int i=0;
        period = new UsagePeriod( s1.getObjectAt( i++ ) );
    }

    protected HeaderRequest getRequest(ASN1Encodable ae) throws ParseException {
        return new HeaderRequestCapability(ae);
    }

    public int getId() {return 1;}

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+Block.CRLF);
        if(period!=null) sb.append( prefix+"  period "+period.dumpValueNotation( prefix+"  " )+Block.CRLF );
        sb.append(prefix+"}");
        return sb.toString();
    }
}
