package net.gwerder.java.mailvortex.test.imap;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

import java.util.logging.Level;
import net.gwerder.java.mailvortex.*;
import net.gwerder.java.mailvortex.imap.*;

/**
 * booleanConfigHandlings for {@link net.gwerder.java.mailvortex.MailVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class CustomKeyManagerTest {

    static {
        MailvortexLogger.setGlobalLogLevel(Level.ALL);
    }    

    @Test
    public void loadKeyStore() {
        try{
            new CustomKeyManager("keystore.jks","changeme", "mykey3");
        } catch(Exception e) {
            fail("Exception should not be thrown");
        }
        try{
            new CustomKeyManager("keystore.jks_does_not_exist","changeme", "mykey3");
            fail("should throw an exception as the keystore does not exist");
        } catch(Exception e) {
            assertTrue("keystore shoult not be found",true);
        }
        try{
            new CustomKeyManager("keystore.jks","BAD_PASSWORD", "mykey3");
            fail("should throw an exception as the password is bad");
        } catch(Exception e) {
            assertTrue("keystore pw is wrong",true);
        }
        try{
            new CustomKeyManager("keystore.jks","changeme", "KEY_DOES_NOT_EXIST");
            fail("should throw an exception as the key alias does not exist");
        } catch(Exception e) {
            assertTrue("keystore content not found",true);
        }
    }

    @Test
    public void unsupportedMethodes() {
        CustomKeyManager ckm=null;
        try{
            ckm=new CustomKeyManager("keystore.jks","changeme", "mykey3");
        } catch(Exception e) {
            fail("Exception should not be thrown");
        }
        try{
            ckm.getClientAliases("",null);
        } catch( UnsupportedOperationException uoe) {
            assertTrue("This is expected to be thrown",true);
        } catch(Exception e) {
            fail("Different Exception expected");
        }
        try{
            ckm.chooseClientAlias(new String[] {""},null,null);
        } catch( UnsupportedOperationException uoe) {
            assertTrue("This is expected to be thrown",true);
        } catch(Exception e) {
            fail("Different Exception expected");
        }
    }
}