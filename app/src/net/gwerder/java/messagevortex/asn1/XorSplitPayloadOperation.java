package net.gwerder.java.messagevortex.asn1;
// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import net.gwerder.java.messagevortex.asn1.encryption.PrngType;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;

/**
 * XorSplitPayloadOperation block.
 *
 * Created by martin.gwerder on 23.05.2017.
 */
public class XorSplitPayloadOperation extends Operation implements Serializable {

    public static final long serialVersionUID = 100000000021L;

    int      originalId;
    PrngType prngSpec;
    int      newFirstBlockId;
    int      newSecondBlockId;


    XorSplitPayloadOperation() {}

    public XorSplitPayloadOperation( ASN1Encodable object ) throws IOException {
        parse(object);
    }

    @Override
    protected void parse( ASN1Encodable to ) throws IOException {
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        int i=0;
        originalId = ASN1Integer.getInstance(s1.getObjectAt( i++ ) ).getValue().intValue();
        prngSpec = PrngType.getById( ASN1Enumerated.getInstance( s1.getObjectAt( i++ ) ).getValue().intValue() );
        newFirstBlockId = ASN1Integer.getInstance(s1.getObjectAt( i++ ) ).getValue().intValue();
        newSecondBlockId = ASN1Integer.getInstance(s1.getObjectAt( i++ ) ).getValue().intValue();
    }

    @Override
    public String dumpValueNotation( String prefix, DumpType dumptype ) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append( '{' ).append( CRLF );
        sb.append( prefix ).append( "  originalId " ).append( originalId ).append( ',' ).append( CRLF );
        sb.append( prefix ).append( "  prngSpec " ).append( prngSpec.name().toLowerCase() ).append( ',' ).append( CRLF );
        sb.append( prefix ).append( "  newFirstBlockId " ).append( newFirstBlockId ).append( ',' ).append( CRLF );
        sb.append( prefix ).append( "  newSecondBlockId " ).append( newSecondBlockId ).append( CRLF );
        sb.append( prefix ).append( '}' );
        return sb.toString();
    }

    @Override
    public ASN1Object toASN1Object( DumpType dumpType ) throws IOException {
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add( new ASN1Integer( originalId ) );
        v.add( new ASN1Enumerated( prngSpec.getId() ) );
        v.add( new ASN1Integer( newFirstBlockId ) );
        v.add( new ASN1Integer( newSecondBlockId ) );
        return new DERSequence( v );
    }

    @Override
    public Operation getNewInstance( ASN1Encodable object ) throws IOException {
        return new XorSplitPayloadOperation(object);
    }
}
