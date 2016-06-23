package net.gwerder.java.mailvortex.test.imap;

import net.gwerder.java.mailvortex.imap.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

/**
 * Tests for {@link net.gwerder.java.mailvortex.imap.ImapException}.
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