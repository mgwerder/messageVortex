package net.messagevortex.test.asn1;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.BlendingParameter;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Created by martin.gwerder on 18.04.2018.
 */
@ExtendWith(GlobalJunitExtension.class)
public class BlendingParameterTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
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
