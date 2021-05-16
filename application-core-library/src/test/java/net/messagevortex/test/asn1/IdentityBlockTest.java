package net.messagevortex.test.asn1;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.HeaderRequest;
import net.messagevortex.asn1.HeaderRequestCapability;
import net.messagevortex.asn1.HeaderRequestIdentity;
import net.messagevortex.asn1.HeaderRequestIncreaseMessageQuota;
import net.messagevortex.asn1.HeaderRequestQueryQuota;
import net.messagevortex.asn1.IdentityBlock;
import net.messagevortex.asn1.encryption.DumpType;
import net.messagevortex.test.GlobalJunitExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.logging.Level;


/**
 * Created by martin.gwerder on 30.05.2016.
 */
@ExtendWith(GlobalJunitExtension.class)
public class IdentityBlockTest {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    /***
     * Reencodes 100 Identities and checks whether their byte and Value Notation Dumps are equivalent.
     */
    @Test
    public void testingIdentityDump1() {
        try {
            for (int i = 0; i < 10; i++) {
                LOGGER.log( Level.INFO, "Testing IdentityBlock reencoding and dumping " + (i + 1) + " of " + 10 );
                IdentityBlock s = new IdentityBlock();
                Assertions.assertTrue(s != null, "IdentityBlock may not be null");
                String s1=s.dumpValueNotation( "" );
                byte[] b1 = s.toBytes(DumpType.ALL_UNENCRYPTED);
                Assertions.assertTrue(b1 != null, "Byte representation may not be null");
                byte[] b2 = (new IdentityBlock( b1 )).toBytes(DumpType.ALL_UNENCRYPTED);
                Assertions.assertTrue(Arrays.equals( b1, b2 ), "Byte arrays should be equal when reencoding");
                String s2 = (new IdentityBlock( b2 )).dumpValueNotation( "" );
                Assertions.assertTrue(s1.equals( s2 ), "Value Notations should be equal when reencoding");

                s.setRequests(new HeaderRequest[] { new HeaderRequestCapability(),new HeaderRequestIdentity(null),new HeaderRequestIncreaseMessageQuota(null),new HeaderRequestQueryQuota(null)});
            }
        } catch (Exception e) {
            LOGGER.log( Level.WARNING,"Unexpected exception",e);
            Assertions.fail("fuzzer encountered exception in IdentityBlock ("+ e +")");
        }
    }

}
