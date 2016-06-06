package net.gwerder.java.mailvortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1TaggedObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.Vector;

/**
 * ASN1 parser class for header request.
 *
 * Created by martin.gwerder on 25.04.2016.
 */
public abstract class HeaderRequest {

    private static final Vector<HeaderRequest> req = new Vector<>();

    static {
        try {
            req.add( new HeaderRequestIdentity() );
            req.add( new HeaderRequestQueryQuota() );
        } catch(Exception e) {}
    }

    protected HeaderRequest() {
    }

    public static HeaderRequest createRequest(ASN1Encodable ae) throws ParseException {
        for(HeaderRequest hr:req) {
            if(hr.getId()==((ASN1TaggedObject)(ae)).getTagNo()) return hr.getRequest(ae);
        }
        return null;
    }

    public ASN1Object toASN1Object() throws IOException {
        throw new IOException( "not implemented" ); //FIXME
    }

    protected abstract HeaderRequest getRequest(ASN1Encodable ae) throws ParseException;

    public abstract int getId();

}
