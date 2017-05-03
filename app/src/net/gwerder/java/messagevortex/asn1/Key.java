package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.Algorithm;
import net.gwerder.java.messagevortex.asn1.encryption.Mode;
import net.gwerder.java.messagevortex.asn1.encryption.Padding;
import net.gwerder.java.messagevortex.asn1.encryption.Parameter;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *Abstract class for all encryption key types
 */
abstract public class Key extends AbstractBlock {


    Algorithm keytype = null;
    HashMap<String, Object> parameters = new HashMap<>();
    byte[] initialisationVector = null;
    Mode mode = null;
    Padding padding = null;

    String getParameterString() {
        StringBuilder sb=new StringBuilder();
        for(Map.Entry<String,Object> e:parameters.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(e.getKey());
            sb.append("=");
            if(e.getValue() instanceof String ) {
                sb.append( (String) e.getValue() );
            } else if(e.getValue() instanceof Integer ) {
                sb.append(e.getValue());
            }
        }
        return sb.toString();
    }

    void parseKeyParameter(ASN1Sequence s) {
        // FIXME may contain multiple keys
        keytype = Algorithm.getById(ASN1Enumerated.getInstance( s.getObjectAt( 0 ) ).getValue().intValue());
        parameters.clear();
        for(ASN1Encodable e: ASN1Sequence.getInstance( s.getObjectAt( 1 ) )) {
            ASN1TaggedObject to=ASN1TaggedObject.getInstance( e );
            Parameter p=Parameter.getById(to.getTagNo());
            if(p==null) {
                Logger.getLogger( "Key" ).log( Level.WARNING, "got unsupported Parameter \"" + ((ASN1TaggedObject) (e)).getTagNo() + "\"" );
            } else if(p.toString().equals(Parameter.IV.toString())) {
                initialisationVector=ASN1OctetString.getInstance( to.getObject() ).getOctets();
            } else if(p.toString().equals(Parameter.PADDING.toString())) {
                padding=Padding.getByName( new String(ASN1OctetString.getInstance( to.getObject() ).getOctets()) );
                if (padding == null) {
                    Logger.getLogger("Key").log(Level.WARNING, "got unsupported Padding \"" + (new String(ASN1OctetString.getInstance(to.getObject()).getOctets())) + "\"");
                }
            } else if(p.toString().equals(Parameter.MODE.toString())) {
                mode=Mode.getByName( new String(ASN1OctetString.getInstance( to.getObject() ).getOctets()) );
            } else if(p.toString().equals(Parameter.CURVETYPE.toString())) {
                parameters.put(Parameter.CURVETYPE.toString()+"_0",new String(ASN1OctetString.getInstance( to.getObject() ).getOctets()));
            } else {
                // determine next free index
                int j = 0;
                while (parameters.get("" + p.getId() + "_" + j) != null) {
                    j++;
                }
                parameters.put("" + p.toString() + "_" + j, ASN1Integer.getInstance( to.getObject() ).getValue().intValue());
            }
        }
    }

    ASN1Encodable encodeKeyParameter() throws IOException {
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
        List<String> vec = new ArrayList<>();
        vec.add(Parameter.PADDING.toString());
        vec.add(Parameter.MODE.toString());
        vec.add(Parameter.IV.toString() );
        for(Map.Entry<String,Object> e:parameters.entrySet()) {
            if( vec.contains( e.getKey().substring(0,e.getKey().length()-2) ) ) {
                // skip all processed entries
            } else if (e.getKey().startsWith(Parameter.KEYSIZE.toString() + "_")) {
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

    String dumpKeyTypeValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append(prefix).append("  keyType {").append(CRLF);
        sb.append(prefix).append("    algorithm " + keytype + ",").append(CRLF);
        sb.append(prefix).append("    parameter {").append(CRLF);
        int i=parameters.size();
        for(Map.Entry<String,Object> e:parameters.entrySet()) {
            i--;
            String[] s=e.getKey().split("_");
            sb.append(prefix).append("      " + s[0] + " " + e.getValue() + (i > 0 ? "," : "")).append(CRLF);
        }
        sb.append(prefix).append("    }").append(CRLF);
        sb.append(prefix).append("  },").append(CRLF);
        return sb.toString();
    }

    abstract public byte[] decrypt(byte[] encrypted) throws IOException;

    abstract public byte[] encrypt(byte[] decrypted) throws IOException;
}
