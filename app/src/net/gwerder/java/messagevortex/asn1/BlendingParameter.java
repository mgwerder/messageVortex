package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;
import java.io.IOException;
import static net.gwerder.java.messagevortex.asn1.BlendingParameter.BlendingParameterChoice.*;

/**
 * Created by martin.gwerder on 15.05.2017.
 */
public class BlendingParameter extends AbstractBlock {
    enum BlendingParameterChoice {
        OFFSET(1),
        SYMMETRIC_KEY(2),
        ASYMMETRIC_KEY(3);

        final int id;

        BlendingParameterChoice(int i) {
            id = i;
        }

        public static BlendingParameterChoice getById(int i) {
            for(BlendingParameterChoice e : values()) {
                if(e.id==i) {
                    return e;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }

    int offset=-1;
    SymmetricKey symmetricKey=null;
    AsymmetricKey asymmetricKey=null;

    public BlendingParameter(ASN1Encodable e ) throws IOException {
        parse(e);
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        ASN1TaggedObject t=ASN1TaggedObject.getInstance(to);
        switch(BlendingParameterChoice.getById(t.getTagNo())) {
            case OFFSET:
                offset=ASN1Integer.getInstance(t.getObject()).getValue().intValue();
                break;
            case ASYMMETRIC_KEY:
                asymmetricKey=new AsymmetricKey(t.getObject().getEncoded());
                break;
            case SYMMETRIC_KEY:
                symmetricKey=new SymmetricKey(t.getObject().getEncoded());
                break;
            default:
                throw new IOException("unknown blending parameter choice detected ("+t.getTagNo()+")");
        }
    }

    public BlendingParameterChoice getChoice() {
        if(offset>-1) {
            return OFFSET;
        } else if(symmetricKey!=null) {
            return SYMMETRIC_KEY;
        } else if(asymmetricKey!=null) {
            return ASYMMETRIC_KEY;
        }
        return null;
    }

    @Override
    public String dumpValueNotation(String prefix, DumpType dumptype) throws IOException {
        StringBuilder sb=new StringBuilder();
        switch(getChoice()) {
            case OFFSET:
                sb.append("offset "+offset);
                break;
            case SYMMETRIC_KEY:
                sb.append("symmetricKey "+symmetricKey.dumpValueNotation(prefix,dumptype));
                break;
            case ASYMMETRIC_KEY:
                sb.append("asymmetricKey " + asymmetricKey.dumpValueNotation(prefix, dumptype));
                break;
            default:
                throw new IOException("unable to dump "+getChoice());
        }
        return sb.toString();
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException {
        switch (getChoice()) {
            case OFFSET:
                return new DERTaggedObject(getChoice().getId(),new ASN1Integer(offset));
            case SYMMETRIC_KEY:
                return new DERTaggedObject(getChoice().getId(),symmetricKey.toASN1Object(dumpType));
            case ASYMMETRIC_KEY:
                return new DERTaggedObject(getChoice().getId(),asymmetricKey.toASN1Object(dumpType));
            default:
                throw new IOException("unable to convert to ASN.1 ("+getChoice()+")");
        }
    }
}
