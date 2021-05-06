package net.messagevortex.test.asn1;
// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.logging.Level;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AbstractBlock;
import net.messagevortex.asn1.IdentityBlock;
import net.messagevortex.asn1.InnerMessageBlock;
import net.messagevortex.asn1.PrefixBlock;
import net.messagevortex.asn1.RoutingCombo;
import net.messagevortex.asn1.SymmetricKey;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.asn1.encryption.DumpType;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for the VortexMessage class.
 *
 * Created by martin.gwerder on 30.05.2016.
 */
@RunWith(JUnit4.class)
public class VortexMessageTest {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel(Level.ALL);
    }

     /***
     * Reencodes 10 Messages and checks whether their byte and Value Notation Dumps are equivalent
     */
    @Test
    public void fuzzingMessage() {
        try {
            final int TESTS=10;
            for (int i = 0; i < TESTS; i++) {
                LOGGER.log( Level.INFO, "Testing VortexMessage reencoding " + (i + 1) + " of " + TESTS );
                PrefixBlock randomOuterPrefixBlock=new PrefixBlock();
                randomOuterPrefixBlock.setKey(new SymmetricKey() );
                IdentityBlock randomIdentityBlock=new IdentityBlock();
                RoutingCombo randomRoutingCombo =new RoutingCombo();
                PrefixBlock randomPrefixBlock=new PrefixBlock();
                VortexMessage s = new VortexMessage(randomOuterPrefixBlock,new InnerMessageBlock( randomPrefixBlock,randomIdentityBlock, randomRoutingCombo));
                String s1=s.dumpValueNotation( "" );
                byte[] b1 = s.toBytes(DumpType.ALL_UNENCRYPTED);
                Assert.assertTrue( "Byte representation may not be null", b1 != null );
                byte[] b2 = (new VortexMessage( b1,null )).toBytes(DumpType.ALL_UNENCRYPTED);
                Assert.assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b1, b2 ) );
                String s2=s.dumpValueNotation( "" );
                Assert.assertTrue( "Value Notations should be equal when reencoding", s1.equals( s2 ) );
            }
        } catch (Exception e) {
            LOGGER.log( Level.WARNING,"Unexpected exception",e);
            Assert.fail( "fuzzer encountered exception in VortexMessage ("+ e +")" );
        }
    }

    @Test
    public void fuzzingIntToByteConverter() {
        LOGGER.log( Level.INFO, "Testing basic byte reencoding length 4");
        testIntByteConverter( 0,4 );
        testIntByteConverter( 1,4 );
        testIntByteConverter( 4294967295L,4 );
        for (int i = 0; i < 100; i++) {
            int rand = (int) (Math.random() * Integer.MAX_VALUE);
            LOGGER.log( Level.INFO, "  Testing byte reencoding " + (i + 1) + " of " + 200 + " [" + rand + "]" );
            testIntByteConverter( rand,4 );
        }
        LOGGER.log( Level.INFO, "Testing basic byte reencoding length 2");
        testIntByteConverter( 0,2 );
        testIntByteConverter( 1,2 );
        testIntByteConverter( 65525L,2 );
        for (int i = 101; i < 200; i++) {
            int rand = (int) (Math.random() * 65535);
            LOGGER.log( Level.INFO, "  Testing byte reencoding " + (i + 1) + " of " + 200 + " [" + rand + "]" );
            testIntByteConverter( rand,2 );
        }
    }

    private void testIntByteConverter(long i, int len) {
        byte[] b= VortexMessage.getLongAsBytes( i,len );
        Assert.assertTrue("wrong number of bytes returned (expected="+len+"; received="+b.length+")",b.length==len);
        Assert.assertTrue( "Error reencoding "+i+ " (old="+i+";"+ AbstractBlock.toHex(b)+";new="+ VortexMessage.getBytesAsLong( b )+"]", VortexMessage.getBytesAsLong( b ) == i );
    }

    @Test
    public void writeAsAsn1() {
        try {
            // FIXME build a full message with all possible blocks
            PrefixBlock randomOuterPrefixBlock=new PrefixBlock();
            randomOuterPrefixBlock.setKey(new SymmetricKey() );
            IdentityBlock randomIdentityBlock=new IdentityBlock();
            RoutingCombo randomRoutingCombo =new RoutingCombo();
            PrefixBlock randomPrefixBlock=new PrefixBlock();
            VortexMessage s = new VortexMessage(randomOuterPrefixBlock,new InnerMessageBlock( randomPrefixBlock,randomIdentityBlock, randomRoutingCombo));
            File f = new File("testfile_VortexMessage_encrypted.der");
            try ( FileOutputStream o = new FileOutputStream(f)) {
                o.write(s.toBytes(DumpType.ALL_UNENCRYPTED));
            }

            try ( FileOutputStream o = new FileOutputStream( new File("testfile_VortexMessage_plain.der" ) ) ) {
                o.write(s.toAsn1Object(DumpType.ALL_UNENCRYPTED).getEncoded());
            }
        } catch (Exception e) {
            Assert.fail("unexpected exception");
        }
    }

    @Test
    public void ByteToLongConversionTest() {
        Assert.assertTrue("error testing byte conversion with 0 ["+VortexMessage.toHex(VortexMessage.getLongAsBytes(0))+"->"+VortexMessage.getBytesAsLong(VortexMessage.getLongAsBytes(0))+"]",0==VortexMessage.getBytesAsLong(VortexMessage.getLongAsBytes(0)));
        Assert.assertTrue("error testing byte conversion with 1",1==VortexMessage.getBytesAsLong(VortexMessage.getLongAsBytes(1)));
        Assert.assertTrue("error testing byte conversion with 1 to bytes", Arrays.equals(new byte[]{1, 0, 0, 0}, VortexMessage.getLongAsBytes(1)));
        Assert.assertTrue("error testing byte conversion with 0 to bytes",Arrays.equals(new byte[] {0,0,0,0},VortexMessage.getLongAsBytes(0)));
        Assert.assertTrue("error testing byte conversion with 256 to bytes",Arrays.equals(new byte[] {0,1,0,0},VortexMessage.getLongAsBytes(256)));
        Assert.assertTrue("error testing byte conversion with 65536 to bytes",Arrays.equals(new byte[] {0,0,1,0},VortexMessage.getLongAsBytes(65536)));
        Assert.assertTrue("error testing byte conversion with "+Long.MAX_VALUE,Long.MAX_VALUE==VortexMessage.getBytesAsLong(VortexMessage.getLongAsBytes(Long.MAX_VALUE,8)));

        // fuzzing value
        for(int i=0;i<1000;i++) {
            long r=(long)(Math.random()+Long.MAX_VALUE);
            Assert.assertTrue( "error fuzzing byte conversion with "+r, r==VortexMessage.getBytesAsLong(VortexMessage.getLongAsBytes(r,8)));
        }
    }

}
