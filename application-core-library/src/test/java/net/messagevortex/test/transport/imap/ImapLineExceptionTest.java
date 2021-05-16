package net.messagevortex.test.transport.imap;

import net.messagevortex.test.GlobalJunitExtension;
import net.messagevortex.transport.imap.ImapException;
import net.messagevortex.transport.imap.ImapLine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests for {@link ImapException}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@ExtendWith(GlobalJunitExtension.class)
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
        Assertions.assertTrue(id==2, "Code coverage for imapException failed");
    }

}
