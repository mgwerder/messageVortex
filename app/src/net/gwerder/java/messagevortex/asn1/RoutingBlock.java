package net.gwerder.java.messagevortex.asn1;

import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by martin.gwerder on 14.04.2016.
 */
public class RoutingBlock extends AbstractBlock {

    private String       recipient = null;
    private UsagePeriod  queueTime = null;
    private List<byte[]> nextHop=new ArrayList<>();

    public RoutingBlock(ASN1Encodable to) throws IOException {
        parse(to);
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
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
        recipient="";

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
        // FIXME this is a dummy to be removed as soon as recipient parsing/encoding is implemented
        recipient.hashCode();
        throw new IOException( "not implemented" ); // FIXME
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        sb.append(prefix+"  -- FIXME RoutingBlock dump not yet implemented"+CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }
}
