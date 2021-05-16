package net.messagevortex.test.transport.imap;

import net.messagevortex.MessageVortex;
import net.messagevortex.test.GlobalJunitExtension;
import net.messagevortex.transport.AuthenticationProxy;
import net.messagevortex.transport.SecurityContext;
import net.messagevortex.transport.SecurityRequirement;
import net.messagevortex.transport.imap.ImapConnection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

import java.io.IOException;
import java.util.Set;

/**
 * Tests for {@link MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@ExtendWith(GlobalJunitExtension.class)
public class AuthenticationProxyTest {

    @Test
    @ResourceLock(value = Resources.SYSTEM_PROPERTIES, mode = ResourceAccessMode.READ_WRITE)
    public void setGetConnection() throws IOException {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        AuthenticationProxy ap=new AuthenticationProxy();
        ap.addUser("Test","Testpw");
        ImapConnection ic=new ImapConnectionDummy() {};
        Assertions.assertTrue(ap.getImapConnection()==null, "ImapConnection should be null if uninited");
        Assertions.assertTrue(ap.setImapConnection(ic)==null, "ImapConnection should be null if uninited");
        Assertions.assertTrue(ap.getImapConnection()==ic, "ImapConnection should return set value");
        Assertions.assertTrue(ap.setImapConnection(null)==ic, "ImapConnection should return set value");
        Assertions.assertTrue(ap.getImapConnection()==null, "ImapConnection should be null if set to null");
        ic.shutdown();
        Assertions.assertTrue(ImapSSLTest.verifyHangingThreads(threadSet).size()==0, "error searching for hangig threads");
    }

    @Test
    public void plainAuthTest() {
        Set<Thread> threadSet = ImapSSLTest.getThreadList();
        AuthenticationProxy ap=new AuthenticationProxy();
        ap.addUser("Test","Testpw");
        Assertions.assertFalse(ap.login(null,"Testpw"), "UserID is null (1)");
        Assertions.assertFalse(ap.login("Test",null), "password is null (1)");
        Assertions.assertFalse(ap.login(null,null), "UserID and password is null (1)");
        Assertions.assertFalse(ap.login("testNotExistingUser","testNotExistingUser"), "UserID does not exist");
        Assertions.assertTrue(ap.login("TEST","Testpw"), "UserID is not handled case insensitive (1)");
        Assertions.assertTrue(ap.login("test","Testpw"), "UserID is not handled case insensitive (2)");
        Assertions.assertFalse(ap.login("test1","Testpw"), "unknown user handling failed");
        Assertions.assertFalse(ap.login("","Testpw"), "unknown user handling failed");
        Assertions.assertFalse(ap.login(null,"Testpw"), "unknown user handling failed");
        Assertions.assertFalse(ap.login("test","Testpw1"), "bad password handling failed (1)");
        Assertions.assertFalse(ap.login("test","TestPW"), "bad password handling failed (1)");
        Assertions.assertFalse(ap.login("test",""), "bad password handling failed (1)");
        Assertions.assertFalse(ap.login("test",null), "bad password handling failed (1)");
        Assertions.assertFalse(ap.login("test","T"), "bad password handling failed (1)");
        Assertions.assertTrue(ImapSSLTest.verifyHangingThreads(threadSet).size()==0, "error searching for hangig threads");
    }

    private static class ImapConnectionDummy extends ImapConnection {
        public ImapConnectionDummy() throws IOException {
            super( null,null );
            setSecurityContext( new SecurityContext(SecurityRequirement.PLAIN) );
            // This is a dummy constructor for test cases do not use it for anything else
        }
    }

}
