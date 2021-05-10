package net.messagevortex.test.asn1;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AbstractBlock;
import net.messagevortex.asn1.encryption.Parameter;
import net.messagevortex.test.GlobalJunitExtension;
import org.bouncycastle.asn1.DERBitString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.logging.Level;

/**
 * General Test class for all unspecific Block tests.
 *
 * Created by martin.gwerder on 30.05.2016.
 */
@ExtendWith(GlobalJunitExtension.class)
public class AbstractBlockTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

    /***
     * Testing null behaviour of toHex().
     */
    @Test
    public void toHexTests() {
        LOGGER.log(Level.INFO,"Testing toHex behaviour");
        Assertions.assertTrue("''H".equals(AbstractBlock.toHex(null)), "toHex(null) is not ''H");
        Assertions.assertTrue("''H".equals(AbstractBlock.toHex(new byte[0])), "toHex({}) is not ''H");
        Assertions.assertTrue("'0D0A'H".equals(AbstractBlock.toHex(new byte[] { '\r','\n'})), "toHex(\"\\r\\n\") is not '0D0A'H");
        Assertions.assertTrue("'00'H".equals(AbstractBlock.toHex(new byte[] { 0 })), "toHex(\"\\0\") is not '00'H");
    }

    /***
     * Testing behaviour of fromHex().
     */
    @Test
    public void fromHexTests() {
        LOGGER.log(Level.INFO,"Testing fromHex behaviour");
        Assertions.assertTrue(AbstractBlock.fromHex(null)==null, "fromHex(null) is not null");
        Assertions.assertTrue(Arrays.equals(new byte[0],AbstractBlock.fromHex( "" )), "fromHex(\"\") is not byte[0]");
        ExtendedSecureRandom esr=new ExtendedSecureRandom();
        for(int i=0;i<100;i++) {
            byte[]arr= ExtendedSecureRandom.generateSeed(257 );
            Assertions.assertTrue(Arrays.equals(arr,AbstractBlock.fromHex( AbstractBlock.toHex(arr) )), "fromHex(\"\") fuzzer rounf "+i);
        }
    }

    /***
     * Testing null behaveour of toBitString()
     */
    @Test
    public void toBitStringTests() {
        LOGGER.log(Level.INFO,"Testing toBitString() behaviour");
        Assertions.assertTrue("''B".equals(AbstractBlock.toBitString(null)), "toBitString(null) is not ''B");
        Assertions.assertTrue("''H".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { },0))), "toBitString(\"\"B) is not ''H ("+ AbstractBlock.toBitString(new DERBitString(new byte[] { },0))+")");
        Assertions.assertTrue("'0D0A'H".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { '\r','\n'},0))), "toBitString(\"\\r\\n\") is not '0D0A'H ("+ AbstractBlock.toBitString(new DERBitString(new byte[] { '\r','\n'},0))+")");
        Assertions.assertTrue("'00'H".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 0 },0))), "toBitString(\"\\0\") is not '00'H ("+ AbstractBlock.toBitString(new DERBitString(new byte[] { 0 },0))+")");
        Assertions.assertTrue("'0'B".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },7))), "toBitString('0'B) is not '0'B ("+ AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },7))+")");
        Assertions.assertTrue("'01'B".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },6))), "toBitString('01'B) is not '01'B");
        Assertions.assertTrue("'010'B".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },5))), "toBitString('010'B) is not '010'B");
        Assertions.assertTrue("'0101'B".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },4))), "toBitString('0101'B) is not '0101'B");
        Assertions.assertTrue("'01010'B".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },3))), "toBitString('01010'B) is not '01010'B");
        Assertions.assertTrue("'010101'B".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },2))), "toBitString('010101'B) is not '010101'B");
        Assertions.assertTrue("'0101010'B".equals(AbstractBlock.toBitString(new DERBitString(new byte[] { 85 },1))), "toBitString('0101010'B) is not '0101010'B");
    }

    /***
     * Testing behaviour of getters
     */
    @Test
    public void parameterGettersTests() {
        Assertions.assertTrue(Parameter.getById(10000)==Parameter.KEYSIZE, "getById(10000)!=KEYSIZE");
        Assertions.assertTrue(Parameter.getById(10001)==Parameter.CURVETYPE, "getById(10001)!=CURVETYPE");
        Assertions.assertTrue(Parameter.getById(0)==null, "getById(0)!=null");
        Assertions.assertTrue(Parameter.getByString("keySize")==Parameter.KEYSIZE, "getByString(10000)!=KEYSIZE");
        Assertions.assertTrue(Parameter.getByString("curveType")==Parameter.CURVETYPE, "getByString(10001)!=CURVETYPE");
        Assertions.assertTrue(Parameter.getByString("curvetype")==null, "getByString(curvetype)!=null");
    }

}
