package net.gwerder.java.messagevortex.asn1.encryption;

import org.bouncycastle.asn1.ASN1Object;

import java.io.IOException;

/**
 * Created by martin.gwerder on 13.05.2017.
 */
public interface Block {

    String dumpValueNotation(String prefix,DumpType dumpType) throws IOException;

    ASN1Object toASN1Object(DumpType dumpType) throws IOException;

    byte[] toBytes(DumpType dumpType) throws IOException;

}
