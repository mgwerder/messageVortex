package net.messagevortex.test.asn1;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.BlendingParameter;
import net.messagevortex.asn1.encryption.DumpType;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.logging.Level;

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
            BlendingParameter s2 = new BlendingParameter(s.toAsn1Object(DumpType.ALL));
            Assert.assertTrue( "Reencoded BlendingParameter is not equal", s2.equals(s) );
        }catch( Exception e) {
            e.printStackTrace();
            Assert.fail( "catched unexpected exception" );
        }
    }

}
