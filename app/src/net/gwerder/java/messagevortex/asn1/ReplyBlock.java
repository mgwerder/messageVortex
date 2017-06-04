package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;

import java.io.IOException;

public abstract class ReplyBlock extends AbstractBlock {
    @Override
    protected abstract void parse(ASN1Encodable to) throws IOException;

    @Override
    public abstract String dumpValueNotation(String prefix,DumpType dumpType) throws IOException;

    @Override
    public abstract ASN1Object toASN1Object(DumpType dumpType) throws IOException;
}
