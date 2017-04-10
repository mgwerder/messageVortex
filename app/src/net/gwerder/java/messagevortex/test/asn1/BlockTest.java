package net.gwerder.java.messagevortex.test.asn1;

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.Block;
import net.gwerder.java.messagevortex.asn1.encryption.Parameter;
import org.bouncycastle.asn1.DERBitString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.logging.Level;

import static org.junit.Assert.assertTrue;

/**
 * General Test class for all unspecific Block tests.
 *
 * Created by martin.gwerder on 30.05.2016.
 */
@RunWith(JUnit4.class)
public class BlockTest {

    private static final java.util.logging.Logger LOGGER;

    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

    @Test
    /***
     * Testing null beheour of toHex()
     */
    public void toHex() {
        assertTrue("toHex(null) is not ''H","''H".equals(Block.toHex(null)));
        assertTrue("toHex({}) is not ''H","''H".equals(Block.toHex(new byte[0])));
        assertTrue("toHex(\"\\r\\n\") is not '0D0A'H","'0D0A'H".equals(Block.toHex(new byte[] { '\r','\n'})));
        assertTrue("toHex(\"\\0\") is not '00'H","'00'H".equals(Block.toHex(new byte[] { 0 })));
    }

    @Test
    /***
     * Testing null behaveour of toBitString()
     */
    public void toBitString() {
        assertTrue("toBitString(null) is not ''B","''B".equals(Block.toBitString(null)));
        assertTrue("toBitString(\"\"B) is not ''H ("+Block.toBitString(new DERBitString(new byte[] { },0))+")","''H".equals(Block.toBitString(new DERBitString(new byte[] { },0))));
        assertTrue("toBitString(\"\\r\\n\") is not '0D0A'H ("+Block.toBitString(new DERBitString(new byte[] { '\r','\n'},0))+")","'0D0A'H".equals(Block.toBitString(new DERBitString(new byte[] { '\r','\n'},0))));
        assertTrue("toBitString(\"\\0\") is not '00'H ("+Block.toBitString(new DERBitString(new byte[] { 0 },0))+")","'00'H".equals(Block.toBitString(new DERBitString(new byte[] { 0 },0))));
        assertTrue("toBitString('0'B) is not '0'B ("+Block.toBitString(new DERBitString(new byte[] { 85 },7))+")","'0'B".equals(Block.toBitString(new DERBitString(new byte[] { 85 },7))));
        assertTrue("toBitString('01'B) is not '01'B","'01'B".equals(Block.toBitString(new DERBitString(new byte[] { 85 },6))));
        assertTrue("toBitString('010'B) is not '010'B","'010'B".equals(Block.toBitString(new DERBitString(new byte[] { 85 },5))));
        assertTrue("toBitString('0101'B) is not '0101'B","'0101'B".equals(Block.toBitString(new DERBitString(new byte[] { 85 },4))));
        assertTrue("toBitString('01010'B) is not '01010'B","'01010'B".equals(Block.toBitString(new DERBitString(new byte[] { 85 },3))));
        assertTrue("toBitString('010101'B) is not '010101'B","'010101'B".equals(Block.toBitString(new DERBitString(new byte[] { 85 },2))));
        assertTrue("toBitString('0101010'B) is not '0101010'B","'0101010'B".equals(Block.toBitString(new DERBitString(new byte[] { 85 },1))));
    }

    @Test
    /***
     * Testing null behaveour of toBitString()
     */
    public void parameterGetters() {
        assertTrue("getById(10000)!=KEYSIZE", Parameter.getById(10000)==Parameter.KEYSIZE);
        assertTrue("getById(10001)!=CURVETYPE",Parameter.getById(10001)==Parameter.CURVETYPE);
        assertTrue("getById(0)!=null",Parameter.getById(0)==null);
        assertTrue("getByString(10000)!=KEYSIZE",Parameter.getByString("keySize")==Parameter.KEYSIZE);
        assertTrue("getByString(10001)!=CURVETYPE",Parameter.getByString("curveType")==Parameter.CURVETYPE);
        assertTrue("getByString(curvetype)!=null",Parameter.getByString("curvetype")==null);
    }

}
