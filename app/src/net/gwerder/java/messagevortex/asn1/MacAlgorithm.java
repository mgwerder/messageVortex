package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.Algorithm;
import net.gwerder.java.messagevortex.asn1.encryption.AlgorithmType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;

import java.io.IOException;

/**
 * Created by martin.gwerder on 05.06.2016.
 */
public class MacAlgorithm extends Block {

    Algorithm alg = null;

    public MacAlgorithm() {
        alg = Algorithm.getDefault( AlgorithmType.HASHING );
    }

    public MacAlgorithm(ASN1Encodable to) throws IOException {
        parse( to );
    }

    public MacAlgorithm(Algorithm a) throws IOException {
        if (a == null || a.getAlgorithmType() != AlgorithmType.HASHING) {
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
