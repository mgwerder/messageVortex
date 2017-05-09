package net.gwerder.java.messagevortex.asn1;
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

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import net.gwerder.java.messagevortex.MessageVortexLogger;
import org.bouncycastle.asn1.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

/**
 * Stores all known identities of a node. Identities are stored as IdentityStoreBlocks.
 ***/
public class IdentityStore extends AbstractBlock {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL );
    }

    private static ExtendedSecureRandom secureRandom = new ExtendedSecureRandom();

    private static IdentityStore demo=null;
    private Map<String, IdentityStoreBlock> blocks = new TreeMap<>();

    public IdentityStore() {
        blocks.clear();
    }

    public IdentityStore(byte[] b) throws IOException {
        this();
        parse( b );
    }

    public IdentityStore(File f) throws IOException {
        this();
        Path asn1DataPath = Paths.get(f.getAbsolutePath());
        byte[] p = Files.readAllBytes(asn1DataPath);
        parse( p );
    }

    public static void resetDemo() {
        demo=null;
    }

    public static IdentityStore getIdentityStoreDemo() throws IOException {
        if (demo == null) {
            demo = getNewIdentityStoreDemo(true);
        }
        return demo;
    }

    public static IdentityStore getNewIdentityStoreDemo(boolean complete) throws IOException {
        IdentityStore tmp = new IdentityStore();
        tmp.add( IdentityStoreBlock.getIdentityStoreBlockDemo( IdentityStoreBlock.IdentityType.OWNED_IDENTITY, complete ) );
        for (int i = 0; i < 100; i++) {
            tmp.add( IdentityStoreBlock.getIdentityStoreBlockDemo( IdentityStoreBlock.IdentityType.NODE_IDENTITY, complete ) );
        }
        for (int i = 0; i < 40; i++){
            tmp.add( IdentityStoreBlock.getIdentityStoreBlockDemo( IdentityStoreBlock.IdentityType.RECIPIENT_IDENTITY, complete ) );
        }
        return tmp;
    }

    public List<IdentityStoreBlock> getAnonSet(int size) throws IOException {
        LOGGER.log( Level.FINE, "Executing getAnonSet("+size+") from "+blocks.size() );
        List<IdentityStoreBlock> ret=new ArrayList<>();
        String[] keys=blocks.keySet().toArray(new String[0]);
        int i=0;
        while(ret.size()<size && i<10000) {
            i++;
            IdentityStoreBlock isb=blocks.get(keys[secureRandom.nextInt(keys.length)]);
            if(isb!=null && isb.getType()==IdentityStoreBlock.IdentityType.RECIPIENT_IDENTITY && !ret.contains( isb ) ) {
                ret.add(isb);
                LOGGER.log( Level.FINER, "adding to anonSet "+isb.getIdentityKey().getPublicKey().hashCode() );
            }
        }
        if(ret.size()<size) {
            throw new IOException("unable to get anon set (size "+size+" too big; achieved: "+ret.size()+")?");
        }
        LOGGER.log( Level.FINE, "done getAnonSet()" );
        return ret;
    }

    protected void parse(byte[] p) throws IOException {
        ASN1InputStream aIn = new ASN1InputStream( p );
        parse( aIn.readObject() );
    }

    public void add(IdentityStoreBlock isb) {
        String ident ="";
        if( isb.getIdentityKey()!=null) {
            ident=toHex(isb.getIdentityKey().getPublicKey());
        }
        blocks.put(ident,isb);
    }

    protected void parse(ASN1Encodable p) throws IOException {
        LOGGER.log( Level.FINER, "Executing parse()" );

        ASN1Sequence s1 = ASN1Sequence.getInstance( p );
        for (ASN1Encodable ao : s1.toArray()) {
            IdentityStoreBlock sb = new IdentityStoreBlock( ao );
            add( sb );
        }
        LOGGER.log( Level.FINER, "Finished parse()" );
    }

    public ASN1Object toASN1Object() throws IOException {
        // Prepare encoding
        LOGGER.log( Level.FINER,"Executing toASN1Object()");

        ASN1EncodableVector v=new ASN1EncodableVector();

        LOGGER.log(Level.FINER,"adding blocks");
        for(Map.Entry<String,IdentityStoreBlock> e:blocks.entrySet()) {
            v.add(e.getValue().toASN1Object());
        }
        ASN1Sequence seq=new DERSequence(v);
        LOGGER.log(Level.FINER,"done toASN1Object()");
        return seq;
    }

    public String dumpValueNotation(String prefix) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append( prefix + "i IdentityStore ::= {" + CRLF );
        sb.append( prefix + "  identities {" +CRLF );
        int i=0;
        for(Map.Entry<String,IdentityStoreBlock> e:blocks.entrySet()) {
            if(i>0) {
                sb.append(","+CRLF);
            }
            sb.append( prefix + "    -- Dumping IdentityBlock "+e.getKey() + CRLF );
            sb.append( prefix + "    "+e.getValue().dumpValueNotation( prefix+"    " ) );
            i++;
        }
        sb.append( CRLF );
        sb.append( prefix + "  }" + CRLF );
        sb.append( prefix + "}" + CRLF );
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        LOGGER.log( Level.INFO, "\n;;; -------------------------------------------" );
        LOGGER.log( Level.INFO, ";;; creating blank dump" );
        IdentityStore m = new IdentityStore();
        LOGGER.log( Level.INFO, m.dumpValueNotation( "" ) );

        LOGGER.log( Level.INFO, "\n;;; -------------------------------------------" );
        LOGGER.log( Level.INFO, ";;; Demo Store Test" );
        IdentityStore m2 = IdentityStore.getIdentityStoreDemo();
        LOGGER.log( Level.INFO, ";;; dumping\r\n"+m2.dumpValueNotation( "" ) );
        LOGGER.log( Level.INFO, ";;; reencode check" );
        LOGGER.log( Level.INFO, ";;;   getting DER stream" );
        byte[] b1 = m.toBytes();
        LOGGER.log( Level.INFO, ";;;   storing to DER stream to " + System.getProperty( "java.io.tmpdir" ) );
        DEROutputStream f = new DEROutputStream( new FileOutputStream( System.getProperty( "java.io.tmpdir" ) + "/temp.der" ) );
        f.writeObject( m.toASN1Object() );
        f.close();
        LOGGER.log( Level.INFO, ";;;   parsing DER stream" );
        IdentityStore m3 = new IdentityStore( b1 );
        LOGGER.log( Level.INFO, ";;;   getting DER stream again" );
        byte[] b2 = m3.toBytes();
        LOGGER.log( Level.INFO, ";;;   comparing" );
        if (Arrays.equals( b1, b2 )) {
            LOGGER.log( Level.INFO, "Reencode success" );
        } else {
            LOGGER.log( Level.INFO, "Reencode FAILED" );
        }
    }

}
