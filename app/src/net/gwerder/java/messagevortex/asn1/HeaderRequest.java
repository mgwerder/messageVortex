package net.gwerder.java.messagevortex.asn1;

import net.gwerder.java.messagevortex.MailvortexLogger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1TaggedObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * ASN1 parser class for header request.
 *
 * Created by martin.gwerder on 25.04.2016.
 */
public abstract class HeaderRequest {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MailvortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    }


    private static final ArrayList<HeaderRequest> req = new ArrayList<>();

    static {
        try {
            req.add( new HeaderRequestIdentity() );
            req.add( new HeaderRequestQueryQuota() );
        } catch(Exception e) {
            LOGGER.log(Level.SEVERE,"Unexpected exception when adding Header Requests in static constructor",e);
        }
    }

    protected HeaderRequest() {
    }

    public static HeaderRequest createRequest(ASN1Encodable ae) throws IOException {
        for(HeaderRequest hr:req) {
            if(hr.getId()==((ASN1TaggedObject)(ae)).getTagNo()) return hr.getRequest(ae);
        }
        return null;
    }

    public ASN1Object toASN1Object() throws IOException {
        throw new IOException( "not implemented" ); //FIXME
    }

    protected abstract HeaderRequest getRequest(ASN1Encodable ae) throws IOException;

    public abstract int getId();

}
