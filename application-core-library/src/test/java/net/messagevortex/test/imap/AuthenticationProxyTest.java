package net.messagevortex.test.imap;

import net.messagevortex.MessageVortex;
import net.messagevortex.transport.AuthenticationProxy;
import net.messagevortex.transport.SecurityContext;
import net.messagevortex.transport.SecurityRequirement;
import net.messagevortex.transport.imap.ImapConnection;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
public class AuthenticationProxyTest {

    @Test
    public void setGetConnection() throws IOException {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        AuthenticationProxy ap=new AuthenticationProxy();
        ap.addUser("Test","Testpw");
        ImapConnection ic=new ImapConnectionDummy() {};
        assertTrue("ImapConnection should be null if uninited",ap.getImapConnection()==null);
        assertTrue("ImapConnection should be null if uninited",ap.setImapConnection(ic)==null);
        assertTrue("ImapConnection should return set value",ap.getImapConnection()==ic);
        assertTrue("ImapConnection should return set value",ap.setImapConnection(null)==ic);
        assertTrue("ImapConnection should be null if set to null",ap.getImapConnection()==null);
        ic.shutdown();
        assertTrue("error searching for hangig threads",ImapSSLTest.verifyHangingThreads(threadSet).size()==0);
    }

    public void plainAuthTest() {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        AuthenticationProxy ap=new AuthenticationProxy();
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
        assertTrue("error searching for hangig threads",ImapSSLTest.verifyHangingThreads(threadSet).size()==0);
    }

    private static class ImapConnectionDummy extends ImapConnection {
        public ImapConnectionDummy() throws IOException {
            super( null,null );
            setSecurityContext( new SecurityContext(SecurityRequirement.PLAIN) );
            // This is a dummy constructor for test cases do not use it for anything else
        }
    }

}
