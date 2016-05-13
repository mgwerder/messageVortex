package net.gwerder.java.mailvortex.asn1;

import org.bouncycastle.asn1.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by martin.gwerder on 23.04.2016.
 */
abstract public class Key extends Block {

    public static enum Algorithm {
        AES128    (1000,AlgorithmType.SYMMETRIC,"aes128"),
        AES192    (1001,AlgorithmType.SYMMETRIC,"aes192"),
        AES256    (1002,AlgorithmType.SYMMETRIC,"aes256"),
        RSA       (2000,AlgorithmType.ASYMMETRIC,"rsa"),
        DSA       (2100,AlgorithmType.ASYMMETRIC,"dsa"),
        SECP384R1 (2500,AlgorithmType.ASYMMETRIC,"secp384R1"),
        SECT409K1 (2501,AlgorithmType.ASYMMETRIC,"sect409K1"),
        SECP521R1 (2502,AlgorithmType.ASYMMETRIC,"secp521r1"),
        SHA384    (3000,AlgorithmType.HASHING,"sha384"),
        SHA512    (3001,AlgorithmType.HASHING,"sha512"),
        TIGER192  (3100,AlgorithmType.HASHING,"tiger192");


        private int id;
        private AlgorithmType t;
        private String txt;

        Algorithm(int id,AlgorithmType t,String txt) {
            this.id=id;
            this.t=t;
            this.txt=txt;
        }

        public int getId() {return id;};

        public static Algorithm[] getAlgorithms(AlgorithmType at) {
            Vector<Algorithm> v=new Vector<Algorithm>();
            for(Algorithm e : values()) {
                if(e.t==at) v.add(e);
            }
            return v.toArray(new Algorithm[v.size()]);
        }

        public static Algorithm getById(int id) {
            for(Algorithm e : values()) {
                if(e.id==id) return e;
            }
            return null;
        }

        public static Algorithm getByString(String s) {
            for(Algorithm e : values()) {
                if(e.toString().equals(s.toLowerCase())) return e;
            }
            return null;
        }

        public String getAlgorithmFamily() {
            return txt.replaceAll("[0-9]*$","");
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    protected Algorithm keytype = null;
    protected HashMap<String,Integer> parameters = new HashMap<String,Integer>();

    protected void parseKeyParameter(ASN1Sequence s) {
        // FIXME may contain multiple keys
        keytype = Algorithm.getById(ASN1Enumerated.getInstance( s.getObjectAt( 0 ) ).getValue().intValue());
        parameters.clear();
        for(ASN1Encodable e: ASN1Sequence.getInstance( s.getObjectAt( 1 ) )) {
            ASN1TaggedObject to=ASN1TaggedObject.getInstance( e );
            Parameter p=Parameter.getById(to.getTagNo());
            //System.out.println("## got parameter "+p.toString());
            if(p==null) {
                Logger.getLogger("Key").log(Level.WARNING,"got unsupported Parameter \""+((ASN1TaggedObject)(e)).getTagNo()+"\"");
            } else {
                int j = 0;
                while (parameters.get("" + p.getId() + "_" + j) != null) j++;
                parameters.put("" + p.getId() + "_" + j, ASN1Integer.getInstance( to.getObject() ).getValue().intValue());
            }
        }
        //System.out.println("## parameter parsing done");
    }

    protected ASN1Encodable encodeKeyParameter() throws IOException {
        ASN1EncodableVector v=new ASN1EncodableVector();
        // FIXME may contain multiple keys
        v.add(new ASN1Enumerated( keytype.getId() ));
        ASN1EncodableVector v2=new ASN1EncodableVector();
        for(Map.Entry<String,Integer> e:parameters.entrySet()) {
            if(e.getKey().startsWith( "10000_" )) { // encoding KeySize
                v2.add(new DERTaggedObject( true,10000,new ASN1Integer( e.getValue().longValue() ) ));
            } else {
                throw new IOException("found new unknown/unencodable parameter \""+e.getKey()+"\"");
            }
        }
        v.add(new DERSequence(v2));
        return new DERSequence( v );
    }

    public ASN1Encodable getASN1() {
        if(keytype==null) return null;
        ASN1EncodableVector ret = new ASN1EncodableVector();
        // keyType
        ret.add(new ASN1Enumerated(keytype.getId()));
        ASN1EncodableVector params=new ASN1EncodableVector();
        for(Map.Entry<String,Integer> e:parameters.entrySet()) {
            String[] s=e.getKey().split("_");
            params.add(new DERTaggedObject(true,Integer.parseInt(s[0]),new ASN1Integer(new BigInteger(""+e.getValue()))) );
        }
        ret.add(new DERSequence(params));
        // FIXME
        return new DERSequence(ret);
    }

    protected String dumpKeyTypeValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append(prefix+"  keyType {"+CRLF);
        sb.append(prefix+"    algorithm "+keytype+","+CRLF);
        sb.append(prefix+"    parameter {"+CRLF);
        int i=parameters.size();
        for(Map.Entry<String,Integer> e:parameters.entrySet()) {
            i--;
            String[] s=e.getKey().split("_");
            sb.append(prefix+"      "+Parameter.getById(Integer.parseInt(s[0]))+" "+e.getValue()+(i>0?",":"")+CRLF);
        }
        sb.append(prefix+"    }"+CRLF);
        sb.append(prefix+"  },"+CRLF);
        return sb.toString();
    }

    abstract public byte[] decrypt(byte[] encrypted) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException,NoSuchProviderException,InvalidKeySpecException,InvalidAlgorithmParameterException;
    abstract public byte[] encrypt(byte[] decrypted) throws NoSuchAlgorithmException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException,NoSuchProviderException,InvalidKeySpecException,InvalidAlgorithmParameterException;
}
