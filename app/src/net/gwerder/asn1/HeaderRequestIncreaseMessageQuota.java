package net.gwerder.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;

import java.text.ParseException;

/**
 * Created by martin.gwerder on 25.04.2016.
 */
public class HeaderRequestIncreaseMessageQuota extends HeaderRequest {

    protected AsymetricKey identity = null;
    protected int quota = -1;

    protected HeaderRequestIncreaseMessageQuota() {super();}

    public HeaderRequestIncreaseMessageQuota(ASN1Encodable ae) throws ParseException {
        parse(ae);
    }

    protected void parse(ASN1Encodable ae) throws ParseException{
        ASN1Sequence s1 = ASN1Sequence.getInstance(ae);
        int i=0;
        identity=new AsymetricKey(s1.getObjectAt(i++));
        // FIXME check integer bounds
        quota = ASN1Integer.getInstance( s1.getObjectAt( i++ ) ).getValue().intValue();
    }

    protected HeaderRequest getRequest(ASN1Encodable ae) throws ParseException {
        return new HeaderRequestIncreaseMessageQuota(ae);
    }

    public int getId() {return 0;}

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+Block.CRLF);
        if(identity!=null) sb.append( prefix+"  identity "+identity.dumpValueNotation( prefix+"  ", AsymetricKey.DumpType.PRIVATE_COMMENTED )+(quota>-1?",":"")+Block.CRLF );
        if(quota>-1) sb.append( prefix+"  quota "+quota+Block.CRLF );
        sb.append(prefix+"}");
        return sb.toString();
    }
}
