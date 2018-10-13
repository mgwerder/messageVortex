package net.messagevortex.test.imap;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.transport.CustomKeyManager;
import net.messagevortex.MessageVortex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * booleanConfigHandlings for {@link MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@RunWith(JUnit4.class)
public class CustomKeyManagerTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }
    static {
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    public void loadKeyStore() {
        try{
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"Loading keystore");
            LOGGER.log(Level.INFO,"************************************************************************");
            new CustomKeyManager("keystore.jks","changeme", "mykey3");
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE,"Unexpected Exception",e);
            fail("Exception should not be thrown ("+e+") in directory "+System.getProperty("user.dir"));
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
            fail("Exception should not be thrown ("+e+")");
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