package net.gwerder.java.mailvortex.test;

import net.gwerder.java.mailvortex.imap.*;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Assert;
import java.util.concurrent.TimeoutException;
import static org.junit.Assert.*;


/**
 * Tests for {@link net.gwerder.java.mailvortex.imap.ImapLineException}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class ImapLineExceptionTest {    
    
    @Test
    public void dummyTest() {
        // improve Test coverage (no testing function possible)
        try{
          new ImapException(null,"test");
          new ImapException(new ImapLine(null,"a b"),"test");
        } catch(ImapException ie) {
          fail("ImapException thrown");
        }
    }
    
}