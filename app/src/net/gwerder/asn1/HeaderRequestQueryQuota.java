package net.gwerder.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;

import java.text.ParseException;

/**
 * Created by martin.gwerder on 25.04.2016.
 */
public class HeaderRequestQueryQuota extends HeaderRequest {

    protected AsymetricKey identity = null;

    protected HeaderRequestQueryQuota() {super();}

    public HeaderRequestQueryQuota(ASN1Encodable ae) throws ParseException {
        parse(ae);
    }

    protected void parse(ASN1Encodable ae) throws ParseException{
        ASN1Sequence s1 = ASN1Sequence.getInstance(ae);
        int i=0;
        identity=new AsymetricKey(s1.getObjectAt(i++));
    }

    protected HeaderRequest getRequest(ASN1Encodable ae) throws ParseException {
        return new HeaderRequestQueryQuota(ae);
    }

    public int getId() {return 4;}

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+Block.CRLF);
        if(identity!=null) sb.append( prefix+"  identity "+identity.dumpValueNotation( prefix+"  ",false ) );
        sb.append(prefix+"}");
        return sb.toString();
    }
}
