package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;

import java.io.IOException;

/**
 * ASN1 parser to request status of current quota.
 *
 * Created by martin.gwerder on 25.04.2016.
 */
public class HeaderRequestQueryQuota extends HeaderRequest {

    protected AsymmetricKey identity = null;

    public HeaderRequestQueryQuota() {super();}

    public HeaderRequestQueryQuota(ASN1Encodable ae) throws IOException {
        this();
        if (ae!=null) {
            parse(ae);
        }
    }

    protected void parse(ASN1Encodable ae) throws IOException {
        ASN1Sequence s1 = ASN1Sequence.getInstance(ae);
        int i=0;
        identity=new AsymmetricKey(s1.getObjectAt(i++));
    }

    protected HeaderRequest getRequest(ASN1Encodable ae) throws IOException {
        return new HeaderRequestQueryQuota(ae);
    }

    public int getId() {return 4;}

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+ AbstractBlock.CRLF);
        if(identity!=null) {
            sb.append( prefix+"  identity "+identity.dumpValueNotation( prefix+"  ", DumpType.PRIVATE_COMMENTED ) );
        }
        sb.append(prefix+"}");
        return sb.toString();
    }
}
