package net.gwerder.java.mailvortex.asn1;

import net.gwerder.java.mailvortex.asn1.encryption.Algorithm;
import net.gwerder.java.mailvortex.asn1.encryption.Parameter;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by martin.gwerder on 23.04.2016.
 */
abstract public class Key extends Block {



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

    private ASN1Encodable getASN1() {
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

    abstract public byte[] decrypt(byte[] encrypted) throws IOException;

    abstract public byte[] encrypt(byte[] decrypted) throws IOException;
}
