package net.gwerder.java.mailvortex.asn1;

import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Vector;

/**
 * Created by martin.gwerder on 14.04.2016.
 */
public class Routing extends Block {

    private String       recipient = null;
    private UsagePeriod  queueTime = null;
    private List<byte[]> nextHop=new Vector<byte[]>();

    public Routing(ASN1Encodable to) throws ParseException {
        parse(to);
    }

    @Override
    protected void parse(ASN1Encodable to) throws ParseException {
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        int i=0;
        recipient=DERIA5String.getInstance(s1.getObjectAt(i++)).getString();
        queueTime=new UsagePeriod(s1.getObjectAt(i++));
        ASN1Sequence as=ASN1Sequence.getInstance( s1.getObjectAt(i++) );
        nextHop.clear();
        for(ASN1Encodable ae:as.toArray()) {
            nextHop.add( ASN1OctetString.getInstance( ae ).getOctets() );
        }

        // FIXME
        // forwardSecret     [102]   ChainSecret OPTIONAL,

        // errorRoutingBlock [103]   RoutingBlock OPTIONAL,
        // replyBlock        [104]   RoutingBlock OPTIONAL,

        // decryptionKey     [100] SEQUENCE (SIZE (1..2)) OF SymetricKey OPTIONAL,
        // encryptionKey     [101] SymetricKey OPTIONAL,

        // cascade           [200] SEQUENCE(SIZE (0..255)) OF CascadeBuildInformation,

    }

    public UsagePeriod getQueueTime() {
        return queueTime;
    }

    public UsagePeriod setQueueTime(UsagePeriod queueTime) {
        UsagePeriod old = this.queueTime;
        this.queueTime = queueTime;
        return old;
    }

    public ASN1Object toASN1Object() throws IOException{
        throw new IOException( "not implemented" ); // FIXME
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        sb.append(prefix+"  -- FIXME Routing dump not yet implemented"+CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }
}
