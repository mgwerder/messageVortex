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

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.AbstractBlock;
import net.messagevortex.asn1.AsymmetricKeyPreCalculator;
import net.messagevortex.asn1.IdentityBlock;
import net.messagevortex.asn1.InnerMessageBlock;
import net.messagevortex.asn1.PrefixBlock;
import net.messagevortex.asn1.RoutingCombo;
import net.messagevortex.asn1.SymmetricKey;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.asn1.encryption.DumpType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.logging.Level;


/**
 * Tests for the VortexMessage class.
 *
 * Created by martin.gwerder on 30.05.2016.
 */
public class VortexMessageTest {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }

     /***
     * Reencodes 10 Messages and checks whether their byte and Value Notation Dumps are equivalent
     */
    @Test
    public void fuzzingMessage() {
        AsymmetricKeyPreCalculator.setCacheFileName("");
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
                Assertions.assertTrue(b1 != null, "Byte representation may not be null");
                byte[] b2 = (new VortexMessage( b1,null )).toBytes(DumpType.ALL_UNENCRYPTED);
                Assertions.assertTrue(Arrays.equals( b1, b2 ), "Byte arrays should be equal when reencoding");
                String s2=s.dumpValueNotation( "" );
                Assertions.assertTrue(s1.equals( s2 ), "Value Notations should be equal when reencoding");
            }
        } catch (Exception e) {
            LOGGER.log( Level.WARNING,"Unexpected exception",e);
            Assertions.fail( "fuzzer encountered exception in VortexMessage ("+ e +")" );
        }
    }

    @Test
    public void fuzzingIntToByteConverter() {
        AsymmetricKeyPreCalculator.setCacheFileName("");
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
        Assertions.assertTrue(b.length==len, "wrong number of bytes returned (expected="+len+"; received="+b.length+")");
        Assertions.assertTrue(VortexMessage.getBytesAsLong( b ) == i, "Error reencoding "+i+ " (old="+i+";"+ AbstractBlock.toHex(b)+";new="+ VortexMessage.getBytesAsLong( b )+"]");
    }

    @Test
    public void writeAsAsn1() {
        AsymmetricKeyPreCalculator.setCacheFileName("");
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
            Assertions.fail("unexpected exception");
        }
    }

    @Test
    public void ByteToLongConversionTest() {
        AsymmetricKeyPreCalculator.setCacheFileName("");
        Assertions.assertTrue(0==VortexMessage.getBytesAsLong(VortexMessage.getLongAsBytes(0)), "error testing byte conversion with 0 ["+VortexMessage.toHex(VortexMessage.getLongAsBytes(0))+"->"+VortexMessage.getBytesAsLong(VortexMessage.getLongAsBytes(0))+"]");
        Assertions.assertTrue(1==VortexMessage.getBytesAsLong(VortexMessage.getLongAsBytes(1)), "error testing byte conversion with 1");
        Assertions.assertTrue(Arrays.equals(new byte[]{1, 0, 0, 0}, VortexMessage.getLongAsBytes(1)), "error testing byte conversion with 1 to bytes");
        Assertions.assertTrue(Arrays.equals(new byte[] {0,0,0,0},VortexMessage.getLongAsBytes(0)), "error testing byte conversion with 0 to bytes");
        Assertions.assertTrue(Arrays.equals(new byte[] {0,1,0,0},VortexMessage.getLongAsBytes(256)), "error testing byte conversion with 256 to bytes");
        Assertions.assertTrue(Arrays.equals(new byte[] {0,0,1,0},VortexMessage.getLongAsBytes(65536)), "error testing byte conversion with 65536 to bytes");
        Assertions.assertTrue(Long.MAX_VALUE==VortexMessage.getBytesAsLong(VortexMessage.getLongAsBytes(Long.MAX_VALUE,8)), "error testing byte conversion with "+Long.MAX_VALUE);

        // fuzzing value
        for(int i=0;i<1000;i++) {
            long r=(long)(Math.random()+Long.MAX_VALUE);
            Assertions.assertTrue(r==VortexMessage.getBytesAsLong(VortexMessage.getLongAsBytes(r,8)), "error fuzzing byte conversion with "+r);
        }
    }

}
