package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;

/**
 * Represents a merge payload operation.
 *
 * This operation joins two payloads to one new payload with size first.length+second.length
 */
public class MergePayloadOperation extends Operation implements Serializable {

    public static final long serialVersionUID = 100000000024L;

    int originalFirstId =-1;
    int originalSecondId=-1;
    int newId=-1;

    MergePayloadOperation() {}

    public MergePayloadOperation(ASN1Encodable object) throws IOException {
        parse(object);
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        ASN1Sequence s1=ASN1Sequence.getInstance(to);
        int i=0;
        originalFirstId= ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
        originalSecondId= ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
        newId= ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
    }

    @Override
    public String dumpValueNotation(String prefix, DumpType dumptype) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        sb.append(prefix+"  originalFirstId "+originalFirstId+","+CRLF);
        sb.append(prefix+"  originalSecondId "+originalSecondId+","+CRLF);
        sb.append(prefix+"  newId "+newId+CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException {
        ASN1EncodableVector s1=new ASN1EncodableVector();
        s1.add(new ASN1Integer(originalFirstId));
        s1.add(new ASN1Integer(originalSecondId));
        s1.add(new ASN1Integer(newId));
        return new DERSequence(s1);
    }

    @Override
    public Operation getNewInstance(ASN1Encodable object) throws IOException {
        return new MergePayloadOperation(object);
    }
}
