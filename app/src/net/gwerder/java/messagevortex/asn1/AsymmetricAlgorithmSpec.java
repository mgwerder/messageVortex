package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.encryption.Algorithm;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by martin.gwerder on 29.12.2017.
 */
public class AsymmetricAlgorithmSpec extends AbstractBlock implements Serializable {

    public static final long serialVersionUID = 100000000003L;

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    Algorithm algorithm;
    AlgorithmParameter parameter;

    public AsymmetricAlgorithmSpec( AsymmetricAlgorithmSpec to ) throws IOException {
        parse( to.toASN1Object(DumpType.ALL) );
    }

    public AsymmetricAlgorithmSpec( ASN1Encodable to ) throws IOException {
        parse( to );
    }

    @Override
    protected void parse( ASN1Encodable to ) throws IOException {
        int i = 0;
        ASN1Sequence s1 = ASN1Sequence.getInstance( to );

        // getting algorithm
        ASN1Enumerated en = ASN1Enumerated.getInstance(s1.getObjectAt(i++));
        algorithm = Algorithm.getById( en.getValue().intValue() );

        // get optional parameters
        if( s1.size() >1 ) {
            parameter = new AlgorithmParameter(s1.getObjectAt(i++));
        }
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public AlgorithmParameter getAlgorithmParameter() {
        return parameter;
    }

    @Override
    public String dumpValueNotation( String prefix, DumpType dumptype ) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append( "{" ).append( CRLF );
        sb.append( prefix ).append( "  " ).append( "algorithm " ).append( algorithm.name().toLowerCase() );
        if ( parameter != null ) {
            sb.append(',').append(CRLF);
            sb.append( prefix ).append( "  " ).append( "parameter " ).append( parameter.dumpValueNotation( prefix+"  ", dumptype ) ).append(CRLF);
        } else {
            sb.append(CRLF);
        }
        sb.append( prefix ).append( '}' );
        return sb.toString();
    }

    @Override
    public ASN1Object toASN1Object( DumpType dumpType ) throws IOException {
        if (algorithm == null) {
            throw new IOException("Algorithm is empty .. unable to create AsymmetricAlgorithmSpec");
        }
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Enumerated(algorithm.getId()));
        if ( parameter != null) {
            v.add( parameter.toASN1Object( dumpType ) );
        }
        return new DERSequence(v);
    }
}
