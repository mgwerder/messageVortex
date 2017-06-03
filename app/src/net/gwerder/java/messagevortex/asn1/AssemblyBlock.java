package net.gwerder.java.messagevortex.asn1;


import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.IOException;

/**
 * Contains all classes extending assembly blocks (Payload operations).
 */
public class AssemblyBlock extends AbstractBlock {

    int   routingBlockIndex = -1;
    int[] payloadBlockIndex = new int[0];

    public AssemblyBlock(ASN1Encodable object) throws IOException {
        parse(object);
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        ASN1Sequence s1= ASN1Sequence.getInstance(to);
        int i=0;
        routingBlockIndex= ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
        ASN1Sequence s2=ASN1Sequence.getInstance(s1.getObjectAt(i++));
        int[] l=new int[s1.size()];
        int j=0;
        for(ASN1Encodable e:s2) {
            l[j++]=ASN1Integer.getInstance(e).getValue().intValue();
        }
        payloadBlockIndex=l;
    }

    @Override
    public String dumpValueNotation(String prefix, DumpType dumptype) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        sb.append(prefix+"  routingBlockIndex "+routingBlockIndex+CRLF);
        sb.append(prefix+"  payloadBlockIndex { ");
        int j=0;
        for(int i:payloadBlockIndex) {
            if(j>0) {
                sb.append(", ");
            }
            sb.append(i);
            j++;
        }
        sb.append(" }");
        return sb.toString();
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException {
        ASN1EncodableVector v=new ASN1EncodableVector();
        v.add(new ASN1Integer(routingBlockIndex));
        ASN1EncodableVector v2=new ASN1EncodableVector();
        for(int i:payloadBlockIndex) {
            v2.add(new ASN1Integer(i));
        }
        v.add(new DERSequence(v2));
        return new DERSequence(v);
    }

}
