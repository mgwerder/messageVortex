package net.gwerder.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1String;

import java.text.ParseException;

/**
 * Created by martin.gwerder on 14.04.2016.
 */
public class Routing extends Block {

    protected String recipient = null;
    protected UsagePeriod queueTime = null;

    public Routing(ASN1Encodable to) throws ParseException {
        parse(to);
    }

    @Override
    protected void parse(ASN1Encodable to) throws ParseException {
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        int i=0;
        recipient=((ASN1String)(s1.getObjectAt(i++))).getString();
        queueTime=new UsagePeriod(s1.getObjectAt(i++));

    }

    @Override
    public ASN1Encodable encodeDER() {
        // FIXME
        return null;
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        sb.append(prefix+"  -- FIXME Routing dump not yet implemented"+CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }
}
