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

import net.gwerder.java.messagevortex.asn1.encryption.CipherUsage;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;


/**
 * Represents a the Blending specification of the cipher specification including usage.
 */
public class CipherSpec extends AbstractBlock implements Serializable {

    public static final long serialVersionUID = 100000000006L;

    private static int SYMMETRIC  = 16001;
    private static int ASYMMETRIC = 16002;
    private static int MAC        = 16003;
    private static int USAGE      = 16004;

    /* The endpoint address to be used */
    private AsymmetricAlgorithmSpec asymmetricSpec = null;
    private SymmetricAlgorithmSpec  symmetricSpec  = null;
    private MacAlgorithmSpec        macSpec        = null;
    private CipherUsage             cipherUsage    = CipherUsage.ENCRYPT;

    /* constructor */
    public CipherSpec(ASN1Encodable to) throws IOException {
        parse(to);
    }

    public CipherSpec(CipherUsage cipherUsage) {
        this.cipherUsage = cipherUsage;
    }

    protected void parse(ASN1Encodable to) throws IOException {
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        int i = 0;

        // parse optional fields sequence
        ASN1TaggedObject to1 = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
        if (to1.getTagNo() == ASYMMETRIC) {
            asymmetricSpec=new AsymmetricAlgorithmSpec(to1.getObject());
            to1 = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
        }
        if (to1.getTagNo() == SYMMETRIC) {
            symmetricSpec=new SymmetricAlgorithmSpec( to1.getObject() );
            to1 = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
        }
        if (to1.getTagNo() == MAC) {
            macSpec=new MacAlgorithmSpec(to1.getObject());
            to1 = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
        }
        if(to1.getTagNo()!=USAGE) {
            throw new IOException("expected USAGE ("+USAGE+") but got "+to1.getTagNo()+" when parsing CipherSpec" );
        }
        cipherUsage =CipherUsage.getById(ASN1Enumerated.getInstance(to1.getObject()).getValue().intValue());

    }

    public AsymmetricAlgorithmSpec getAsymmetricSpec() {
        return asymmetricSpec;
    }

    public AsymmetricAlgorithmSpec setAsymmetricSpec( AsymmetricAlgorithmSpec spec ) {
        AsymmetricAlgorithmSpec ret = this.asymmetricSpec;
        this.asymmetricSpec = spec;
        return ret;
    }

    public SymmetricAlgorithmSpec getSymmetricSpec() {
        return symmetricSpec;
    }

    public SymmetricAlgorithmSpec setSymmetricSpec( SymmetricAlgorithmSpec spec ) {
        SymmetricAlgorithmSpec ret = this.symmetricSpec;
        this.symmetricSpec = spec;
        return ret;
    }

    public MacAlgorithmSpec getMacSpec() {
        return macSpec;
    }

    public MacAlgorithmSpec setMacSpec( MacAlgorithmSpec spec ) {
        MacAlgorithmSpec ret = this.macSpec;
        this.macSpec = spec;
        return ret;
    }

    public CipherUsage getCipherUsage() {
        return cipherUsage;
    }

    public CipherUsage setCipherUsage( CipherUsage usage ) {
        CipherUsage ret = cipherUsage;
        this.cipherUsage = usage;
        return ret;
    }

    @Override
    public String dumpValueNotation(String prefix, DumpType dumpType) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append( "{" ).append( CRLF );
        if( asymmetricSpec != null ) {
            sb.append(prefix).append("  ").append("asymmetric ").append( asymmetricSpec.dumpValueNotation(prefix + "  ", dumpType ) ).append( ',' ).append( CRLF );
        }
        if( symmetricSpec != null ) {
            sb.append(prefix).append("  ").append("symmetric ").append( symmetricSpec.dumpValueNotation( prefix + "  ", dumpType ) ).append( ',' ).append( CRLF );
        }
        if( macSpec != null ) {
            sb.append(prefix).append("  ").append("mac ").append( macSpec.dumpValueNotation( prefix + "  ", dumpType ) ).append( ',' ).append( CRLF );
        }
        sb.append(prefix).append("  ").append("cipherUsage ").append( cipherUsage.getUsageString() ).append( CRLF );
        sb.append( prefix ).append( '}' );
        return sb.toString();
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException {
        if ( cipherUsage == null) {
            throw new IOException("CipherSpec is empty .. unable to create CipherSpec");
        }
        ASN1EncodableVector v = new ASN1EncodableVector();
        if( asymmetricSpec != null ) {
            v.add( new DERTaggedObject( ASYMMETRIC, asymmetricSpec.toASN1Object( dumpType ) ) );
        }
        if( symmetricSpec != null ) {
            v.add( new DERTaggedObject( SYMMETRIC, symmetricSpec.toASN1Object( dumpType ) ) );
        }
        if( macSpec != null ) {
            v.add( new DERTaggedObject( MAC, macSpec.toASN1Object( dumpType ) ) );
        }
        v.add( new DERTaggedObject( USAGE, new ASN1Enumerated( cipherUsage.getId() ) ) );
        return new DERSequence(v);
    }
}
