package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by Martin on 04.06.2017.
 */
public class SizeBlock extends AbstractBlock  implements Serializable {

    public static final long serialVersionUID = 100000000015L;

    public SizeBlock(ASN1Encodable o) throws IOException {
        parse(o);
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        throw new NotImplementedException(); // FIXME
    }

    @Override
    public String dumpValueNotation(String prefix, DumpType dumptype) throws IOException {
        throw new NotImplementedException(); // FIXME
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException {
        throw new NotImplementedException(); // FIXME
    }
}
