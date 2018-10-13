package net.gwerder.java.messagevortex.test.asn1;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.AbstractBlock;
import net.gwerder.java.messagevortex.asn1.AsymmetricKey;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

/***
 * Test class for writing sample files
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class BuildSamplesTest {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }


    private static final String CRLF = "\r\n";

    public static void writeFile(AbstractBlock b, String asn1type, DumpType dt, String filename, String description ) throws IOException {
        writeFile( b.toBytes(dt), "SAMPLE_derbinary_"+filename+".der" );
        writeFile( ( "Sample "+asn1type+" ::= " + b.dumpValueNotation( "   ", dt ) + CRLF ).getBytes(), "SAMPLE_value_"+filename+".txt" );
        // FIXME add compilation test
    }

    public static void writeFile(byte[] b, String filename ) throws IOException {
        File f = new File( filename );
        FileOutputStream fos = new FileOutputStream( f );
        fos.write( b );
        fos.close();
    }

    @Test
    public void buildIdentityBlock() throws IOException {
        LOGGER.log(Level.INFO,"writing asymmetric key files");
        AsymmetricKey ak = new AsymmetricKey();
        writeFile( ak, "AsymmetricKey", DumpType.ALL, "asymmetric_key_unencrypted", "An full asymmetric key" );
        writeFile( ak, "AsymmetricKey", DumpType.PUBLIC_ONLY, "asymmetric_key_public", "An asymmetric key only containing the public key" );
    }
}
