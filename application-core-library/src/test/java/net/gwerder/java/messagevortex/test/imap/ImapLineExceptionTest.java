package net.gwerder.java.messagevortex.test.imap;

import net.gwerder.java.messagevortex.transport.imap.ImapException;
import net.gwerder.java.messagevortex.transport.imap.ImapLine;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link net.gwerder.java.messagevortex.transport.imap.ImapException}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapLineExceptionTest {

    @Test
    public void dummyTest() {
        // improve Test coverage (no testing function possible)
        int id=0;
        try{
          throw new ImapException(null,"test");
        } catch(ImapException ie) {
          id++;
        }
        try{
          throw new ImapException(new ImapLine(null,"a b"),"test");
        } catch(ImapException ie) {
          id++;
        }
        assertTrue("Code coverage for imapException failed", id==2);
    }

}
