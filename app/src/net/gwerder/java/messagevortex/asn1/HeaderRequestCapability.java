package net.gwerder.java.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;

import java.io.IOException;

/**
 * ASN1 parser block for the capability request.
 *
 * Created by martin.gwerder on 25.04.2016.
 */
public class HeaderRequestCapability extends HeaderRequest {

    protected UsagePeriod period = null;

    public HeaderRequestCapability() {super();}

    public HeaderRequestCapability(ASN1Encodable ae) throws IOException {
        this();
        if (ae!=null) {
            parse(ae);
        }
    }

    protected void parse(ASN1Encodable ae) throws IOException{
        ASN1Sequence s1 = ASN1Sequence.getInstance(ae);
        int i=0;
        period = new UsagePeriod( s1.getObjectAt( i++ ) );
    }

    protected HeaderRequest getRequest(ASN1Encodable ae) throws IOException {
        return new HeaderRequestCapability(ae);
    }

    public int getId() {return 1;}

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+ AbstractBlock.CRLF);
        if (period != null) {
            sb.append(prefix).append("  period ").append(period.dumpValueNotation(prefix + "  ")).append(AbstractBlock.CRLF);
        }
        sb.append(prefix).append("}");
        return sb.toString();
    }
}
