package net.messagevortex.test.asn1;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AbstractBlock;
import net.messagevortex.asn1.encryption.Parameter;
import org.bouncycastle.asn1.DERBitString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;

/**
 * General Test class for all unspecific Block tests.
 *
 * Created by martin.gwerder on 30.05.2016.
 */
@RunWith(JUnit4.class)
public class AbstractBlockTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    /***
     * Testing null behaviour of toHex().
     */
    @Test
    public void toHexTest() {
        LOGGER.log(Level.INFO,"Testing toHex behaviour");
        assertTrue("toHex(null) is not ''H","''H".equals(AbstractBlock.toHex(null)));
        assertTrue("toHex({}) is not ''H","''H".equals(AbstractBlock.toHex(new byte[0])));
        assertTrue("toHex(\"\\r\\n\") is not '0D0A'H","'0D0A'H".equals(AbstractBlock.toHex(new byte[] { '\r','\n'})));
        assertTrue("toHex(\"\\0\") is not '00'H","'00'H".equals(AbstractBlock.toHex(new byte[] { 0 })));
    }

    /***
     * Testing behaviour of fromHex().
     */
    @Test
    public void fromHexTest() {
        LOGGER.log(Level.INFO,"Testing fromHex behaviour");
        assertTrue("fromHex(null) is not null",AbstractBlock.fromHex(null)==null);
        assertTrue("fromHex(\"\") is not byte[0]", Arrays.equals(new byte[0],AbstractBlock.fromHex( "" )));
        ExtendedSecureRandom esr=new ExtendedSecureRandom();
        for(int i=0;i<100;i++) {
            byte[]arr=esr.generateSeed(257 );
            assertTrue("fromHex(\"\") fuzzer rounf "+i, Arrays.equals(arr,AbstractBlock.fromHex( AbstractBlock.toHex(arr) )));
        }
    }

    /***
     * Testing null behaveour of toBitString()
     */
    @Test
    public void toBitStringTest() {
        LOGGER.log(Level.INFO,"Testing toBitString() behaviour");
        assertTrue("toBitString(null) is not ''B","''B".equals(AbstractBlock.toBitString(null)));
        assertTrue("toBitString(\"\"B) is not ''H ("+ AbstractBlock.toBitString(new DERBitString(new byte[] { },0))+")","''H".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { },0))));
        assertTrue("toBitString(\"\\r\\n\") is not '0D0A'H ("+ AbstractBlock.toBitString(new DERBitString(new byte[] { '\r','\n'},0))+")","'0D0A'H".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { '\r','\n'},0))));
        assertTrue("toBitString(\"\\0\") is not '00'H ("+ AbstractBlock.toBitString(new DERBitString(new byte[] { 0 },0))+")","'00'H".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 0 },0))));
        assertTrue("toBitString('0'B) is not '0'B ("+ AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },7))+")","'0'B".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },7))));
        assertTrue("toBitString('01'B) is not '01'B","'01'B".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },6))));
        assertTrue("toBitString('010'B) is not '010'B","'010'B".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },5))));
        assertTrue("toBitString('0101'B) is not '0101'B","'0101'B".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },4))));
        assertTrue("toBitString('01010'B) is not '01010'B","'01010'B".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },3))));
        assertTrue("toBitString('010101'B) is not '010101'B","'010101'B".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },2))));
        assertTrue("toBitString('0101010'B) is not '0101010'B","'0101010'B".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },1))));
    }

    /***
     * Testing behaviour of getters
     */
    @Test
    public void parameterGettersTest() {
        assertTrue("getById(10000)!=KEYSIZE", Parameter.getById(10000)==Parameter.KEYSIZE);
        assertTrue("getById(10001)!=CURVETYPE",Parameter.getById(10001)==Parameter.CURVETYPE);
        assertTrue("getById(0)!=null",Parameter.getById(0)==null);
        assertTrue("getByString(10000)!=KEYSIZE",Parameter.getByString("keySize")==Parameter.KEYSIZE);
        assertTrue("getByString(10001)!=CURVETYPE",Parameter.getByString("curveType")==Parameter.CURVETYPE);
        assertTrue("getByString(curvetype)!=null",Parameter.getByString("curvetype")==null);
    }

}
