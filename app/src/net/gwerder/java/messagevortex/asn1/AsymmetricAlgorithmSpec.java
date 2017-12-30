package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by martin.gwerder on 29.12.2017.
 */
public class AsymmetricAlgorithmSpec extends AbstractBlock implements Serializable {

    public static final long serialVersionUID = 100000000003L;

    public AsymmetricAlgorithmSpec(ASN1Encodable to) throws IOException {
        parse(to);
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        // FIXME implementation missing
    }

    @Override
    public String dumpValueNotation(String prefix, DumpType dumptype) throws IOException {
        // FIXME implementation missing
        return null;
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException {
        // FIXME implementation missing
        return null;
    }
}
