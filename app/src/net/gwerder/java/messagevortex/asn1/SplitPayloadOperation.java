package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;

/**
 * Splits a payload block in two blocks.
 */
public class SplitPayloadOperation extends Operation implements Serializable {

    public static final long serialVersionUID = 100000000023L;

    int originalFirstId=-1;
    int originalSecondId=-1;
    SizeBlock originalSize=null;
    int newId=-1;

    SplitPayloadOperation() {}

    public SplitPayloadOperation(ASN1Encodable object) throws IOException {
        parse(object);
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        ASN1Sequence s1=ASN1Sequence.getInstance(to);
        int i=0;
        originalFirstId= ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
        originalSecondId= ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
        originalSize=new SizeBlock(s1.getObjectAt(i++));
        newId= ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
    }

    @Override
    public String dumpValueNotation(String prefix, DumpType dumptype) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append( '{' ).append( CRLF );
        sb.append( prefix ).append( "  originalFirstId " ).append( originalFirstId ).append( ',' ).append( CRLF );
        sb.append( prefix ).append( "  originalSecondId " ).append( originalSecondId ).append( ',' ).append( CRLF );
        sb.append( prefix ).append( "  originalSize " ).append( originalSize.dumpValueNotation( prefix+"  ", dumptype ) ).append( ',' ).append( CRLF );
        sb.append( prefix ).append( "  newId " ).append( newId ).append( CRLF );
        sb.append( prefix ).append( '}' );
        return sb.toString();
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException {
        ASN1EncodableVector s1=new ASN1EncodableVector();
        s1.add(new ASN1Integer(originalFirstId));
        s1.add(new ASN1Integer(originalSecondId));
        s1.add(originalSize.toASN1Object(dumpType));
        s1.add(new ASN1Integer(newId));
        return new DERSequence(s1);
    }

    @Override
    public Operation getNewInstance(ASN1Encodable object) throws IOException {
        return new SplitPayloadOperation(object);
    }
}
