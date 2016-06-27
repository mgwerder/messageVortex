package net.gwerder.java.mailvortex.test.routing;

import net.gwerder.java.mailvortex.MailvortexLogger;
import net.gwerder.java.mailvortex.asn1.IdentityStore;
import net.gwerder.java.mailvortex.asn1.IdentityStoreBlock;
import net.gwerder.java.mailvortex.routing.GraphSet;
import net.gwerder.java.mailvortex.routing.Graph;
import net.gwerder.java.mailvortex.routing.MessageFactory;
import org.bouncycastle.asn1.DEROutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by martin.gwerder on 13.06.2016.
 */
@RunWith(JUnit4.class)
public class MessageFactoryTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MailvortexLogger.setGlobalLogLevel( Level.ALL);
    }

    @Test
    public void simpleMessageFactoryTest() throws IOException {
        LOGGER.log( Level.INFO, "getting example store" );
        IdentityStore is = null;
        try {
            is = new IdentityStore( new File( System.getProperty( "java.io.tmpdir" ) + "/IdentityStoreExample1.der" ) );
        } catch (Exception ioe) {
            is = IdentityStore.getNewIdentityStoreDemo( false );
            DEROutputStream f = new DEROutputStream( new FileOutputStream( System.getProperty( "java.io.tmpdir" ) + "/IdentityStoreExample1.der" ) );
            f.writeObject( is.toASN1Object() );
            f.close();
        }
        int maxTests=100;
        for (int i = 1; i <= maxTests; i++) {
            LOGGER.log( Level.INFO, "cycle "+i+" of "+maxTests );
            LOGGER.log( Level.INFO, "  building message ("+i+" of "+maxTests+")" );
            MessageFactory smf = MessageFactory.buildMessage( "Subject: This is the message subject\n\nhello", 0, 1, is.getAnonSet( 30 ).toArray( new IdentityStoreBlock[0] ), is );
            smf.build();
            GraphSet gs = smf.getGraph();
            for (Graph gt : gs) {
                assertTrue( "unreached endpoint", gs.targetReached( gt.getFrom() ) && gs.targetReached( gt.getTo() ) );
            }
            LOGGER.log( Level.INFO, "  getting routes ("+i+" of "+maxTests+")" );
            GraphSet[] g = gs.getRoutes();
            if(g == null || g.length == 0) {
                gs.dump();
                fail( "Routes not found (" + g + "/" + (g != null ? g.length : 0) + ")" );
            }
            LOGGER.log( Level.INFO, "  testing full GraphSet ("+i+" of "+maxTests+")" );
            for (GraphSet gt : g) {
                for (Graph gt2 : gt) {
                    assertTrue( "unreached endpoint", gt.targetReached( gt2.getFrom() ) && gt.targetReached( gt2.getTo() ) );
                }
            }
        }
    }

}