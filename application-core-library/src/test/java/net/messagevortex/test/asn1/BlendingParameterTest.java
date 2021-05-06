package net.messagevortex.test.asn1;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.BlendingParameter;
import net.messagevortex.asn1.encryption.DumpType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;

/**
 * Created by martin.gwerder on 18.04.2018.
 */
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
            Assertions.assertTrue(s2.equals(s), "Reencoded BlendingParameter is not equal");
        }catch( Exception e) {
            e.printStackTrace();
            Assertions.fail( "catched unexpected exception" );
        }
    }

}
