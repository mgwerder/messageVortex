package net.gwerder.java.messagevortex.test.asn1;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.AlgorithmParameter;
import net.gwerder.java.messagevortex.asn1.AsymmetricAlgorithmSpec;
import net.gwerder.java.messagevortex.asn1.BlendingParameter;
import net.gwerder.java.messagevortex.asn1.CipherSpec;
import net.gwerder.java.messagevortex.asn1.encryption.Algorithm;
import net.gwerder.java.messagevortex.asn1.encryption.CipherUsage;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.logging.Level;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

/**
 * Created by martin.gwerder on 18.04.2018.
 */
@RunWith( JUnit4.class)
public class BlendingParameterTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void reencodingBlendingParameter() {
        try {
            BlendingParameter s = new BlendingParameter( BlendingParameter.BlendingParameterChoice.OFFSET );
            BlendingParameter s2 = new BlendingParameter(s.toASN1Object(DumpType.ALL));
            assertTrue( "Reencoded BlendingParameter is not equal", s2.equals(s) );
        }catch( Exception e) {
            e.printStackTrace();
            fail( "catched unexpected exception" );
        }
    }

}
