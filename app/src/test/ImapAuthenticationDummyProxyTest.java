package net.gwerder.java.mailvortex.test;
 
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Assert;
import java.util.concurrent.TimeoutException;
import static org.junit.Assert.*;

import net.gwerder.java.mailvortex.imap.*;

/**
 * Tests for {@link net.gwerder.java.mailvortex.MailVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapAuthenticationDummyProxyTest {

    @Test
    public void plainAuthTest() {
        ImapAuthenticationDummyProxy ap=new ImapAuthenticationDummyProxy();
        ap.addUser("Test","Testpw");
        assertTrue("UserID is not handled case insensitive (1)",ap.login("TEST","Testpw"));
        assertTrue("UserID is not handled case insensitive (2)",ap.login("test","Testpw"));
        assertFalse("unknown user handling failed",ap.login("test1","Testpw"));
        assertFalse("unknown user handling failed",ap.login("","Testpw"));
        assertFalse("unknown user handling failed",ap.login(null,"Testpw"));
        assertFalse("bad password handling failed (1)",ap.login("test","Testpw1"));
        assertFalse("bad password handling failed (1)",ap.login("test","TestPW"));
        assertFalse("bad password handling failed (1)",ap.login("test",""));
        assertFalse("bad password handling failed (1)",ap.login("test",null));
        assertFalse("bad password handling failed (1)",ap.login("test","T"));
    }    

}