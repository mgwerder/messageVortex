package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Object;

import java.io.IOException;

/**
 * Common interface for any ASN.1 block.
 */
public interface Block {

    String dumpValueNotation(String prefix,DumpType dumpType) throws IOException;

    ASN1Object toASN1Object(DumpType dumpType) throws IOException;

    byte[] toBytes(DumpType dumpType) throws IOException;

}
