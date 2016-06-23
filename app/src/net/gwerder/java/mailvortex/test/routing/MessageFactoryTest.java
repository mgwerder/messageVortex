package net.gwerder.java.mailvortex.test.routing;

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

import static org.junit.Assert.assertTrue;

/**
 * Created by martin.gwerder on 13.06.2016.
 */
@RunWith(JUnit4.class)
public class MessageFactoryTest {

    @Test
    public void simpleMessageFactoryTest() throws IOException  {
        IdentityStore is=null;
        try {
            is = new IdentityStore( new File(System.getProperty( "java.io.tmpdir" ) + "/IdentityStoreExample1.der") );
        } catch( IOException ioe ){
            is = IdentityStore.getNewIdentityStoreDemo( false );
            DEROutputStream f = new DEROutputStream( new FileOutputStream( System.getProperty( "java.io.tmpdir" ) + "/IdentityStoreExample1.der" ) );
            f.writeObject( is.toASN1Object() );
            f.close();
        }
        MessageFactory smf= MessageFactory.buildMessage( "Subject: This is the message subject\n\nhello", 0, 1, is.getAnonSet( 30 ).toArray( new IdentityStoreBlock[0] ), is );
        smf.build();
        GraphSet gs=smf.getGraph();
        for(Graph gt:gs) {
            assertTrue("unreached endpoint",gs.targetReached( gt.getFrom() ) && gs.targetReached( gt.getTo() ));
        }
        GraphSet[] g=gs.getRoutes();
        assertTrue("Routes not found",g!=null && g.length>0);
        for(GraphSet gt:g) {
            for(Graph gt2:gt) {
                assertTrue("unreached endpoint",gt.targetReached( gt2.getFrom() ) && gt.targetReached( gt2.getTo() ));
            }
        }
    }

}