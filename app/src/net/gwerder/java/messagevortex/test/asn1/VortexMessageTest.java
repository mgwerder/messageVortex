package net.gwerder.java.messagevortex.test.asn1;
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

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.*;
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.logging.Level;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

    static{
        // start key precalculator
        AsymmetricKeyPreCalculator.setCacheFileName("AsymmetricKey.cache");
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
                PrefixBlock p=new PrefixBlock();
                p.setKey(new SymmetricKey() );
                VortexMessage s = new VortexMessage(p,new InnerMessageBlock( new IdentityBlock() ));
                String s1=s.dumpValueNotation( "" );
                byte[] b1 = s.toBytes();
                assertTrue( "Byte representation may not be null", b1 != null );
                byte[] b2 = (new VortexMessage( b1,null )).toBytes();
                assertTrue( "Byte arrays should be equal when reencoding", Arrays.equals( b1, b2 ) );
                String s2=s.dumpValueNotation( "" );
                assertTrue( "Value Notations should be equal when reencoding", s1.equals( s2 ) );
            }
        } catch (Exception e) {
            LOGGER.log( Level.WARNING,"Unexpected exception",e);
            fail( "fuzzer encountered exception in VortexMessage ("+e.toString()+")" );
        }
    }

    @Test
    public void fuzzingIntToByteConverter() {
        LOGGER.log( Level.INFO, "Testing basic byte reencoding length 4");
        testIntByteConverter( 0,4 );
        testIntByteConverter( 1,4 );
        testIntByteConverter( 4294967295l,4 );
        for (int i = 0; i < 100; i++) {
            int rand = (int) (Math.random() * Integer.MAX_VALUE);
            LOGGER.log( Level.INFO, "  Testing byte reencoding " + (i + 1) + " of " + 200 + " [" + rand + "]" );
            testIntByteConverter( rand,4 );
        }
        LOGGER.log( Level.INFO, "Testing basic byte reencoding length 2");
        testIntByteConverter( 0,2 );
        testIntByteConverter( 1,2 );
        testIntByteConverter( 65525l,2 );
        for (int i = 101; i < 200; i++) {
            int rand = (int) (Math.random() * 65535);
            LOGGER.log( Level.INFO, "  Testing byte reencoding " + (i + 1) + " of " + 200 + " [" + rand + "]" );
            testIntByteConverter( rand,2 );
        }
    }

    private void testIntByteConverter(long i, int len) {
        byte[] b= VortexMessage.getLongAsBytes( i,len );
        assertTrue("wrong number of bytes returned (expected="+len+"; received="+b.length+")",b.length==len);
        assertTrue( "Error reencoding "+i+ " (old="+i+";"+ AbstractBlock.toHex(b)+";new="+ VortexMessage.getBytesAsLong( b )+"]", VortexMessage.getBytesAsLong( b ) == i );
    }

    @Test
    public void writeAsAsn1() {
        try {
            // FIXME build a full message with all possible blocks
            PrefixBlock p=new PrefixBlock();
            p.setKey(new SymmetricKey() );
            VortexMessage s = new VortexMessage(p,new InnerMessageBlock( new IdentityBlock() ));
            File f = new File("testfile_VortexMessage_encrypted.der");
            OutputStream o = new FileOutputStream(f);
            o.write(s.toBytes());
            o.close();

            f = new File("testfile_VortexMessage_plain.der");
            o = new FileOutputStream(f);
            o.write(s.toASN1Object(null,DumpType.ALL_UNENCRYPTED).getEncoded());
            o.close();
        } catch (Exception e) {
            fail("unexpected exception");
        }
    }

    @Test
    public void ByteToLongConversionTest() {
        assertTrue("error testing byte conversion with 0 ["+VortexMessage.toHex(VortexMessage.getLongAsBytes(0))+"->"+VortexMessage.getBytesAsLong(VortexMessage.getLongAsBytes(0))+"]",0==VortexMessage.getBytesAsLong(VortexMessage.getLongAsBytes(0)));
        assertTrue("error testing byte conversion with 1",1==VortexMessage.getBytesAsLong(VortexMessage.getLongAsBytes(1)));
        assertTrue("error testing byte conversion with 1 to bytes",Arrays.equals(new byte[] {1,0,0,0},VortexMessage.getLongAsBytes(1)));
        assertTrue("error testing byte conversion with 0 to bytes",Arrays.equals(new byte[] {0,0,0,0},VortexMessage.getLongAsBytes(0)));
        assertTrue("error testing byte conversion with 256 to bytes",Arrays.equals(new byte[] {0,1,0,0},VortexMessage.getLongAsBytes(256)));
        assertTrue("error testing byte conversion with 65536 to bytes",Arrays.equals(new byte[] {0,0,1,0},VortexMessage.getLongAsBytes(65536)));
        assertTrue("error testing byte conversion with "+Long.MAX_VALUE,Long.MAX_VALUE==VortexMessage.getBytesAsLong(VortexMessage.getLongAsBytes(Long.MAX_VALUE,8)));

        // fuzzing value
        for(int i=0;i<1000;i++) {
            long r=(long)(Math.random()+Long.MAX_VALUE);
            assertTrue( "error fuzzing byte conversion with "+r, r==VortexMessage.getBytesAsLong(VortexMessage.getLongAsBytes(r,8)));
        }
    }

}
