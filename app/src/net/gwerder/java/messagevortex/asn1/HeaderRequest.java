package net.gwerder.java.messagevortex.asn1;
// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import net.gwerder.java.messagevortex.MessageVortexLogger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1TaggedObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import static net.gwerder.java.messagevortex.asn1.AbstractBlock.CRLF;

/**
 * ASN1 parser class for header request.
 *
 * Created by martin.gwerder on 25.04.2016.
 */
public abstract class HeaderRequest extends AbstractBlock {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
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
            if(hr.getId()==((ASN1TaggedObject)(ae)).getTagNo()) {
                return hr.getRequest(ae);
            }
        }
        return null;
    }

    public ASN1Object toASN1Object() throws IOException {
        throw new IOException( "not implemented" ); //FIXME
    }

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append(prefix+"-- FIXME dumping of Request object not yet supported"+CRLF);
        return sb.toString();
    }


    protected abstract HeaderRequest getRequest(ASN1Encodable ae) throws IOException;

    public abstract int getId();

}
