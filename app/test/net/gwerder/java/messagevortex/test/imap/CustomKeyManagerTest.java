package net.gwerder.java.messagevortex.test.imap;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.imap.CustomKeyManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * booleanConfigHandlings for {@link net.gwerder.java.messagevortex.MailVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class CustomKeyManagerTest {

    static {
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
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
