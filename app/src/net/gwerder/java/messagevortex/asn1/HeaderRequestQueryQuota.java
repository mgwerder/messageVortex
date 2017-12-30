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
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;

/**
 * ASN1 parser to request status of current quota.
 */
public class HeaderRequestQueryQuota extends HeaderRequest  implements Serializable {

    public static final long serialVersionUID = 100000000025L;

    protected AsymmetricKey identity = null;

    public HeaderRequestQueryQuota() {super();}

    public HeaderRequestQueryQuota(ASN1Encodable ae) throws IOException {
        this();
        if (ae!=null) {
            parse(ae);
        }
    }

    protected void parse(ASN1Encodable ae) throws IOException {
        ASN1Sequence s1 = ASN1Sequence.getInstance(ae);
        int i=0;
        identity=new AsymmetricKey(toDER(s1.getObjectAt(i++).toASN1Primitive()));
    }

    protected HeaderRequest getRequest(ASN1Encodable ae) throws IOException {
        return new HeaderRequestQueryQuota(ae);
    }

    public int getId() {return 4;}

    @Override
    public String dumpValueNotation(String prefix,DumpType dumpType) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+ CRLF);
        if(identity!=null) {
            sb.append( prefix+"  identity "+identity.dumpValueNotation( prefix+"  ", DumpType.PRIVATE_COMMENTED ) );
        }
        sb.append(prefix+"}");
        return sb.toString();
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException {
        ASN1EncodableVector s1=new ASN1EncodableVector();
        s1.add(identity.toASN1Object(dumpType));
        return new DERSequence(s1);
    }
}
