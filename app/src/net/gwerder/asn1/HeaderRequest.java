package net.gwerder.asn1;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1TaggedObject;

import java.text.ParseException;
import java.util.Vector;

/**
 * Created by martin.gwerder on 25.04.2016.
 */
public abstract class HeaderRequest {

    private static final Vector<HeaderRequest> req = new Vector<HeaderRequest>();

    static {
        try {
            req.add( new HeaderRequestIdentity() );
            req.add( new HeaderRequestQueryQuota() );
        } catch(Exception e) {}
    }

    public static HeaderRequest createRequest(ASN1Encodable ae) throws ParseException {
        for(HeaderRequest hr:req) {
            if(hr.getId()==((ASN1TaggedObject)(ae)).getTagNo()) return hr.getRequest(ae);
        }
        return null;
    }

    protected HeaderRequest() {};

    protected abstract HeaderRequest getRequest(ASN1Encodable ae) throws ParseException;

    public abstract int getId();

}
