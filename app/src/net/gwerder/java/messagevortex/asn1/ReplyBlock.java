package net.gwerder.java.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

public class ReplyBlock extends AbstractBlock {
    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public String dumpValueNotation(String prefix) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    ASN1Object toASN1Object() throws IOException, NoSuchAlgorithmException, ParseException {
        throw new NotImplementedException();
    }
}
