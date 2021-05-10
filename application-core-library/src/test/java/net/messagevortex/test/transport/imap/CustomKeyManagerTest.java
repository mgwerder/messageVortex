package net.messagevortex.test.transport.imap;

import net.messagevortex.MessageVortex;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.test.GlobalJunitExtension;
import net.messagevortex.transport.CustomKeyManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.logging.Level;

/**
 * booleanConfigHandlings for {@link MessageVortex}.
 *
 * @author martin@gwerder.net (Martin GWERDER)
 */
@ExtendWith(GlobalJunitExtension.class)
public class CustomKeyManagerTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    @Test
    @DisplayName("Testing successful loading of keystore")
    public void loadKeyStoreTests() {
        try{
            LOGGER.log(Level.INFO,"************************************************************************");
            LOGGER.log(Level.INFO,"Loading keystore");
            LOGGER.log(Level.INFO,"************************************************************************");
            new CustomKeyManager("keystore.jks","changeme", "mykey3");
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE,"Unexpected Exception",e);
            Assertions.fail("Exception should not be thrown ("+e+") in directory "+System.getProperty("user.dir"));
        }
        try{
            new CustomKeyManager("keystore.jks_does_not_exist","changeme", "mykey3");
            Assertions.fail("should throw an exception as the keystore does not exist");
        } catch(Exception e) {
            Assertions.assertTrue(true, "keystore shoult not be found");
        }
        try{
            new CustomKeyManager("keystore.jks","BAD_PASSWORD", "mykey3");
            Assertions.fail("should throw an exception as the password is bad");
        } catch(Exception e) {
            Assertions.assertTrue(true, "keystore pw is wrong");
        }
        try{
            new CustomKeyManager("keystore.jks","changeme", "KEY_DOES_NOT_EXIST");
            Assertions.fail("should throw an exception as the key alias does not exist");
        } catch(Exception e) {
            Assertions.assertTrue(true, "keystore content not found");
        }
    }

    @Test
    public void unsupportedMethodes() {
        CustomKeyManager ckm=null;
        try{
            ckm=new CustomKeyManager("keystore.jks","changeme", "mykey3");
        } catch(Exception e) {
            Assertions.fail("Exception should not be thrown ("+e+")");
        }
        try{
            ckm.getClientAliases("",null);
        } catch( UnsupportedOperationException uoe) {
            Assertions.assertTrue(true, "This is expected to be thrown");
        } catch(Exception e) {
            Assertions.fail("Different Exception expected");
        }
        try{
            ckm.chooseClientAlias(new String[] {""},null,null);
        } catch( UnsupportedOperationException uoe) {
            Assertions.assertTrue(true, "This is expected to be thrown");
        } catch(Exception e) {
            Assertions.fail("Different Exception expected");
        }
    }
}
