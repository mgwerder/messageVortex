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

import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;

import java.io.IOException;

/**
 * ASN1 parser for increasing message quota.
 *
 * Created by martin.gwerder on 25.04.2016.
 */
public class HeaderRequestIncreaseMessageQuota extends HeaderRequest {

    private AsymmetricKey identity = null;
    private long quota = -1;

    public HeaderRequestIncreaseMessageQuota() {super();}

    public HeaderRequestIncreaseMessageQuota(ASN1Encodable ae) throws IOException {
        this();
        if (ae!=null) {
            parse(ae);
        }
    }

    protected void parse(ASN1Encodable ae) throws IOException{
        ASN1Sequence s1 = ASN1Sequence.getInstance(ae);
        int i=0;
        identity=new AsymmetricKey(s1.getObjectAt(i++));
        // FIXME check integer bounds
        quota = ASN1Integer.getInstance( s1.getObjectAt( i++ ) ).getValue().intValue();
    }

    protected HeaderRequest getRequest(ASN1Encodable ae) throws IOException {
        return new HeaderRequestIncreaseMessageQuota(ae);
    }

    public long getQuota() {
        return quota;
    }

    public int getId() {return 0;}

    public String dumpValueNotation(String prefix) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+ AbstractBlock.CRLF);
        if(identity!=null) {
            sb.append( prefix+"  identity "+identity.dumpValueNotation( prefix+"  ", DumpType.PRIVATE_COMMENTED )+(quota>-1?",":"")+ AbstractBlock.CRLF );
        }
        if(quota>-1) {
            sb.append( prefix+"  quota "+quota+ AbstractBlock.CRLF );
        }
        sb.append(prefix+"}");
        return sb.toString();
    }
}
