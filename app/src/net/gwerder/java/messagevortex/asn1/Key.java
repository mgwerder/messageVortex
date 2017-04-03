package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.Algorithm;
import net.gwerder.java.messagevortex.asn1.encryption.Mode;
import net.gwerder.java.messagevortex.asn1.encryption.Padding;
import net.gwerder.java.messagevortex.asn1.encryption.Parameter;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by martin.gwerder on 23.04.2016.
 */
abstract public class Key extends Block {



    protected Algorithm keytype = null;
    protected HashMap<String,Object> parameters = new HashMap<>();
    protected byte[] initialisationVector = null;
    protected Mode mode = null;
    protected Padding padding = null;

    protected String getParameterString() {
        StringBuilder sb=new StringBuilder();
        for(Map.Entry<String,Object> e:parameters.entrySet()) {
            if(sb.length()>0) sb.append(", ");
            sb.append(e.getKey());
            sb.append("=");
            if(e.getValue() instanceof String ) {
                sb.append( (String) e.getValue() );
            } else if(e.getValue() instanceof Integer ) {
                sb.append( (Integer) e.getValue() );
            }
        }
        return sb.toString();
    }

    protected void parseKeyParameter(ASN1Sequence s) {
        // FIXME may contain multiple keys
        keytype = Algorithm.getById(ASN1Enumerated.getInstance( s.getObjectAt( 0 ) ).getValue().intValue());
        parameters.clear();
        for(ASN1Encodable e: ASN1Sequence.getInstance( s.getObjectAt( 1 ) )) {
            ASN1TaggedObject to=ASN1TaggedObject.getInstance( e );
            Parameter p=Parameter.getById(to.getTagNo());
            //System.out.println("## got parameter "+p.toString());
            if(p==null) {
                Logger.getLogger( "Key" ).log( Level.WARNING, "got unsupported Parameter \"" + ((ASN1TaggedObject) (e)).getTagNo() + "\"" );
            } else if(p.toString().equals(Parameter.IV.toString())) {
                initialisationVector=ASN1OctetString.getInstance( to.getObject() ).getOctets();
            } else if(p.toString().equals(Parameter.PADDING.toString())) {
                padding=Padding.getByName( new String(ASN1OctetString.getInstance( to.getObject() ).getOctets()) );
                if(padding==null) Logger.getLogger( "Key" ).log( Level.WARNING, "got unsupported Padding \"" + (new String(ASN1OctetString.getInstance( to.getObject() ).getOctets())) + "\"" );
            } else if(p.toString().equals(Parameter.MODE.toString())) {
                mode=Mode.getByName( new String(ASN1OctetString.getInstance( to.getObject() ).getOctets()) );
            } else if(p.toString().equals(Parameter.CURVETYPE.toString())) {
                parameters.put(Parameter.CURVETYPE.toString()+"_0",new String(ASN1OctetString.getInstance( to.getObject() ).getOctets()));
            } else {
                int j = 0;
                while (parameters.get("" + p.getId() + "_" + j) != null) j++;
                parameters.put("" + p.toString() + "_" + j, ASN1Integer.getInstance( to.getObject() ).getValue().intValue());
            }
        }
        //System.out.println("## parameter parsing done");
    }

    protected ASN1Encodable encodeKeyParameter() throws IOException {
        ASN1EncodableVector v=new ASN1EncodableVector();
        // FIXME may contain multiple keys
        v.add(new ASN1Enumerated( keytype.getId() ));
        ASN1EncodableVector v2=new ASN1EncodableVector();
        // add IV
        if(initialisationVector!=null && initialisationVector.length>0 ) {
            v2.add(new DERTaggedObject( true, Parameter.IV.getId(), new DEROctetString( initialisationVector) ));
        }
        // add padding
        if(padding!=null ) {
            v2.add(new DERTaggedObject( true, Parameter.PADDING.getId(), new DEROctetString( padding.getPadding().getBytes()) ));
        }
        // add mode
        if(mode!=null ) {
            v2.add(new DERTaggedObject( true, Parameter.MODE.getId(), new DEROctetString( mode.toString().getBytes()) ));
        }
        // add custom parameters
        Vector<String> vec=new Vector<>();
        vec.add(Parameter.PADDING.toString());
        vec.add(Parameter.MODE.toString());
        vec.add(Parameter.IV.toString() );
        for(Map.Entry<String,Object> e:parameters.entrySet()) {
            if( vec.contains( e.getKey().substring(0,e.getKey().length()-2) ) ) {
                // skip all processed entries
            } else if(e.getKey().startsWith( Parameter.KEYSIZE.toString()+"_" )) { // encoding KeySize
                v2.add( new DERTaggedObject( true, Parameter.KEYSIZE.getId(), new ASN1Integer( ((Integer) (e.getValue())).longValue() ) ) );
            } else if(e.getKey().equals( Parameter.CURVETYPE.toString()+"_0" )) {
                v2.add( new DERTaggedObject( true, Parameter.CURVETYPE.getId(), new DEROctetString( ((String)(e.getValue())).getBytes() )));
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
        for(Map.Entry<String,Object> e:parameters.entrySet()) {
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
        for(Map.Entry<String,Object> e:parameters.entrySet()) {
            i--;
            String[] s=e.getKey().split("_");
            sb.append(prefix+"      "+s[0]+" "+e.getValue()+(i>0?",":"")+CRLF);
        }
        sb.append(prefix+"    }"+CRLF);
        sb.append(prefix+"  },"+CRLF);
        return sb.toString();
    }

    abstract public byte[] decrypt(byte[] encrypted) throws IOException;

    abstract public byte[] encrypt(byte[] decrypted) throws IOException;
}
