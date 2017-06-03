package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.IOException;

/**
 * Created by Martin on 03.06.2017.
 */
public abstract class AbstractCryptPayloadOperation extends Operation  {

    int originalId=-1;
    SymmetricKey key=null;
    int newId=-1;

    AbstractCryptPayloadOperation() {}

    public AbstractCryptPayloadOperation(ASN1Encodable object) throws IOException {
        parse(object);
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        ASN1Sequence s1=ASN1Sequence.getInstance(to);
        int i=0;
        originalId= ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
        key=new SymmetricKey(s1.getObjectAt(i++).toASN1Primitive().getEncoded());
        newId= ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
    }

    @Override
    public String dumpValueNotation(String prefix, DumpType dumptype) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        sb.append(prefix+"  originalId "+originalId+","+CRLF);
        sb.append(prefix+"  key "+key.dumpValueNotation(prefix+"  ",dumptype)+","+CRLF);
        sb.append(prefix+"  newId "+newId+CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException {
        ASN1EncodableVector v=new ASN1EncodableVector();
        v.add(new ASN1Integer(originalId));
        v.add(key.toASN1Object(dumpType));
        v.add(new ASN1Integer(newId));
        return new DERSequence(v);
    }

    public abstract Operation getNewInstance(ASN1Encodable object) throws IOException ;
}
