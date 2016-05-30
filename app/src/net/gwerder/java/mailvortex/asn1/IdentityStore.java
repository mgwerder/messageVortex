package net.gwerder.java.mailvortex.asn1;

import org.bouncycastle.asn1.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by martin.gwerder on 26.05.2016.
 */
public class IdentityStore extends Block {

    private Map<String,IdentityStoreBlock> blocks=new TreeMap<String,IdentityStoreBlock>();
    private static IdentityStore demo=null;

    public IdentityStore() {
        blocks.clear();
    }

    public IdentityStore(byte[] b) throws IOException,ParseException,NoSuchAlgorithmException {
        this();
        parse( b );
    }

    protected void parse(byte[] p) throws IOException,ParseException,NoSuchAlgorithmException {
        ASN1InputStream aIn=new ASN1InputStream( p );
        parse(aIn.readObject());
    }

    public static void resetDemo() {
        demo=null;
    }

    public static IdentityStore getIdentityStoreDemo() throws IOException {
        if(demo==null) {
            IdentityStore tmp=new IdentityStore(  );
            tmp.add(IdentityStoreBlock.getIdentityStoreBlockDemo( IdentityStoreBlock.IdentityType.OWNED_IDENTITY,true ));
            for(int i=0;i<400;i++) tmp.add(IdentityStoreBlock.getIdentityStoreBlockDemo( IdentityStoreBlock.IdentityType.NODE_IDENTITY,true ));
            for(int i=0;i<100;i++) tmp.add(IdentityStoreBlock.getIdentityStoreBlockDemo( IdentityStoreBlock.IdentityType.RECIPIENT_IDENTITY,true ));
            demo=tmp;
        }

        return demo;
    }

    public void add(IdentityStoreBlock isb) {
        String ident ="";
        if( isb.getIdentityKey()!=null) ident=toHex(isb.getIdentityKey().getPublicKey());
        blocks.put(ident,isb);
    }

    protected void parse(ASN1Encodable p) throws IOException,ParseException,NoSuchAlgorithmException {
        Logger.getLogger( "IdentityStore" ).log( Level.FINER, "Executing parse()" );

        ASN1Sequence s1 = ASN1Sequence.getInstance( p );
        for (ASN1Encodable ao : s1.toArray()) {
            IdentityStoreBlock sb = new IdentityStoreBlock( ao );
            add( sb );
        }
        Logger.getLogger( "IdentityStore" ).log( Level.FINER, "Finished parse()" );
    }

    public ASN1Object toASN1Object() throws IOException {
        // Prepare encoding
        Logger.getLogger("IdentityStore").log( Level.FINER,"Executing toASN1Object()");

        ASN1EncodableVector v=new ASN1EncodableVector();

        Logger.getLogger("IdentityStore").log(Level.FINER,"adding blocks");
        for(Map.Entry<String,IdentityStoreBlock> e:blocks.entrySet()) {
            v.add(e.getValue().toASN1Object());
        }
        ASN1Sequence seq=new DERSequence(v);
        Logger.getLogger("IdentityStore").log(Level.FINER,"done toASN1Object()");
        return seq;
    }

    public String dumpValueNotation(String prefix) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append( prefix + "i IdentityStore ::= {" + CRLF );
        sb.append( prefix + "  identities {" +CRLF );
        int i=0;
        for(Map.Entry<String,IdentityStoreBlock> e:blocks.entrySet()) {
            if(i>0) sb.append(","+CRLF);
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
        System.out.println("\n;;; -------------------------------------------");
        System.out.println(";;; creating blank dump");
        IdentityStore m=new IdentityStore();
        System.out.println(m.dumpValueNotation(""));

        System.out.println("\n;;; -------------------------------------------");
        System.out.println(";;; Demo Store Test");
        IdentityStore m2=IdentityStore.getIdentityStoreDemo();
        System.out.println(";;; dumping");
        System.out.println(m2.dumpValueNotation(""));
        System.out.println(";;; reencode check");
        System.out.println(";;;   getting DER stream");
        byte[] b1=m.toBytes();
        System.out.println(";;;   storing to DER stream to "+System.getProperty("java.io.tmpdir"));
        DEROutputStream f=new DEROutputStream( new FileOutputStream(System.getProperty("java.io.tmpdir")+"/temp.der") );
        f.writeObject(m.toASN1Object());
        f.close();
        System.out.println(";;;   parsing DER stream");
        IdentityStore m3=new IdentityStore(b1);
        System.out.println(";;;   getting DER stream again");
        byte[] b2=m3.toBytes();
        System.out.println(";;;   comparing");
        if(Arrays.equals(b1,b2) ) {
            System.out.println( "Reencode success" );
        } else {
            System.out.println( "Reencode FAILED" );
        }
    }

}
