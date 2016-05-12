package net.gwerder.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;

import java.text.ParseException;

/**
 * Created by martin.gwerder on 25.04.2016.
 */
public class HeaderRequestIdentity extends HeaderRequest {

    protected UsagePeriod period = null;
    protected AsymmetricKey identity = null;

    protected HeaderRequestIdentity() {super();}

    public HeaderRequestIdentity(ASN1Encodable ae) throws ParseException {
        parse(ae);
    }

    protected void parse(ASN1Encodable ae) throws ParseException{
        ASN1Sequence s1 = ASN1Sequence.getInstance(ae);
        int i=0;
        identity=new AsymmetricKey(s1.getObjectAt(i++));
        period = new UsagePeriod( s1.getObjectAt( i++ ) );
    }

    protected HeaderRequest getRequest(ASN1Encodable ae) throws ParseException {
        return new HeaderRequestIdentity(ae);
    }

    public int getId() {return 0;}

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+Block.CRLF);
        if(identity!=null) sb.append( prefix+"  identity "+identity.dumpValueNotation( prefix+"  ", AsymmetricKey.DumpType.PRIVATE_COMMENTED )+Block.CRLF );
        if(period!=null)   sb.append( prefix+"  period "+period.dumpValueNotation( prefix+"  " )+identity!=null?",":""+Block.CRLF );
        sb.append(prefix+"}");
        return sb.toString();
    }
}
