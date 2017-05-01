package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.Algorithm;
import net.gwerder.java.messagevortex.asn1.encryption.AlgorithmType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;

import java.io.IOException;

/**
 * TODO
 */
public class MacAlgorithm extends Block {

    Algorithm alg = null;

    public MacAlgorithm() {
        alg = Algorithm.getDefault( AlgorithmType.HASHING );
    }

    /***
     * constructor to creates a mac algorith from an ASN.1 encoded object.
     *
     * @param to            the object description in ASN.1 notation
     * @throws IOException  if an error occures during parsing
     * @throws NullPointerException if object is null
     */
    public MacAlgorithm(ASN1Encodable to) throws IOException {
        if (to == null) {
            throw new NullPointerException( "object may not be null" );
        }
        parse( to );
    }

    /***
     * constructor to creates a mac algorith from an ASN.1 encoded object.
     *
     * @param a             the object description in ASN.1 notation
     * @throws IOException  if an error occures during parsing
     * @throws NullPointerException if object is null
     */
    public MacAlgorithm(Algorithm a) throws IOException {
        if (a == null ) {
            throw new NullPointerException( "object may not be null" );
        }
        if( a.getAlgorithmType() != AlgorithmType.HASHING) {
            throw new IOException( "Only hashing algorithms allowed" );
        }
        alg = a;
    }

    protected void parse(ASN1Encodable to) throws IOException {
        Algorithm a = Algorithm.getById( ASN1Integer.getInstance( to ).getValue().intValue() );
        if (a == null || a.getAlgorithmType() != AlgorithmType.HASHING){
            throw new IOException( "Only hashing algorithms allowed" );
        }
        alg = a;
    }

    public ASN1Object toASN1Object() {
        return new ASN1Integer( alg.getId() );
    }

    public String dumpValueNotation(String prefix) {
        return "" + alg.getId();
    }

    public Algorithm setAlgorithm(Algorithm alg) throws IOException {
        if (alg.getAlgorithmType() != AlgorithmType.HASHING) {
            throw new IOException( "Only hashing algorithms allowed" );
        }
        Algorithm old = this.alg;
        this.alg = alg;
        return old;
    }

    public Algorithm getAlgorithm() {
        return alg;
    }

}
