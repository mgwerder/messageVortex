package net.gwerder.asn1;

import org.bouncycastle.asn1.*;

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

    protected void parseKeyParameter(ASN1Encodable kt, ASN1Encodable param) {
        // FIXME may contain multiple keys
        keytype = Algorithm.getById(ASN1Enumerated.getInstance(kt).getValue().intValue());
        parameters.clear();
        for(ASN1Encodable e: ASN1Sequence.getInstance(param)) {
            Parameter p=Parameter.getById(((ASN1TaggedObject)(e)).getTagNo());
            if(p==null) {
                Logger.getLogger("Key").log(Level.WARNING,"got unsupported Parameter \""+((ASN1TaggedObject)(e)).getTagNo()+"\"");
            } else {
                int j = 0;
                while (parameters.get("" + p.getId() + "_" + j) != null) j++;
                parameters.put("" + p.getId() + "_" + j, ((ASN1Integer) (((ASN1TaggedObject) (e)).getObject())).getValue().intValue());
            }
        }
    }

    public ASN1Encodable encodeDER() {
        if(keytype==null) return null;
        ASN1EncodableVector ret = new ASN1EncodableVector();
        // keyType
        ret.add(new ASN1Enumerated(keytype.getId()));
        ASN1EncodableVector params=new ASN1EncodableVector();
        for(Map.Entry<String,Integer> e:parameters.entrySet()) {
            String[] s=e.getKey().split("_");
            params.add(new DERTaggedObject(false,Integer.parseInt(s[0]),new ASN1Integer(new BigInteger(""+e.getValue()))) );
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

}
