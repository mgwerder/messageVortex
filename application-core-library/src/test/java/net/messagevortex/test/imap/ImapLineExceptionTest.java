package net.messagevortex.test.imap;

import net.messagevortex.transport.imap.ImapException;
import net.messagevortex.transport.imap.ImapLine;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link ImapException}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
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
