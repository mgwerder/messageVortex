package net.gwerder.java.messagevortex.test.imap;

import net.gwerder.java.messagevortex.imap.ImapAuthenticationDummyProxy;
import net.gwerder.java.messagevortex.imap.ImapConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link net.gwerder.java.messagevortex.MailVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapAuthenticationDummyProxyTest {

    @Test
    public void setGetConnection() {
        ImapAuthenticationDummyProxy ap=new ImapAuthenticationDummyProxy();
        ap.addUser("Test","Testpw");
        ImapConnection ic=new ImapConnectionDummy() {};
        assertTrue("ImapConnection should be null if uninited",ap.getImapConnection()==null);
        assertTrue("ImapConnection should be null if uninited",ap.setImapConnection(ic)==null);
        assertTrue("ImapConnection should return set value",ap.getImapConnection()==ic);
        assertTrue("ImapConnection should return set value",ap.setImapConnection(null)==ic);
        assertTrue("ImapConnection should be null if set to null",ap.getImapConnection()==null);
    }

    public void plainAuthTest() {
        ImapAuthenticationDummyProxy ap=new ImapAuthenticationDummyProxy();
        ap.addUser("Test","Testpw");
        assertFalse("UserID is null (1)",ap.login(null,"Testpw"));
        assertFalse("password is null (1)",ap.login("Test",null));
        assertFalse("UserID and password is null (1)",ap.login(null,null));
        assertFalse("UserID does not exist",ap.login("testNotExistingUser","testNotExistingUser"));
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

    private static class ImapConnectionDummy extends ImapConnection {
        public ImapConnectionDummy() {
            // This is a dummy constructor for test cases do not use it for anything else
        }
    }

}