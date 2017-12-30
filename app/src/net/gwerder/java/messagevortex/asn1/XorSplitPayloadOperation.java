package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by martin.gwerder on 23.05.2017.
 */
public class XorSplitPayloadOperation extends Operation implements Serializable {

    public static final long serialVersionUID = 100000000021L;

    XorSplitPayloadOperation() {}

    public XorSplitPayloadOperation(ASN1Encodable object) throws IOException {
        parse(object);
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public String dumpValueNotation(String prefix, DumpType dumptype) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public Operation getNewInstance(ASN1Encodable object) throws IOException {
        return new XorSplitPayloadOperation(object);
    }
}
