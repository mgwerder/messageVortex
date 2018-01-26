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
import net.gwerder.java.messagevortex.asn1.encryption.Parameter;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * ASN1 parser block for algorithm parameters.
 *
 * Created by martin.gwerder on 25.04.2016.
 */
public class AlgorithmParameter extends AbstractBlock implements Serializable,Comparable<AlgorithmParameter> {

    public static final long serialVersionUID = 100000000001L;

    private Map<Integer,String> parameter;

    public AlgorithmParameter() {
        parameter=new ConcurrentSkipListMap<>();
    }

    public AlgorithmParameter(ASN1Encodable ae) throws IOException {
        this();
        if (ae!=null) {
            parse(ae);
        }
    }

    public AlgorithmParameter(AlgorithmParameter p) {
        for(Map.Entry<Integer,String> e:p.parameter.entrySet()) {
            put(e.getKey(),new String (e.getValue()));
        }
    }

    public String put(String id,String value) {
        return put(Parameter.getByString(id).getId(),value);
    }

    public String put(int id,String value) {
        if(value==null) {
            String ret=get(id);
            parameter.remove(id);
            return ret;
        } else {
            return parameter.put(id,value);
        }
    }

    public String put(Parameter parameter,String value) {
        // this assertion catches rewritten keysizes (different values)
        assert parameter!=Parameter.KEYSIZE || (get(parameter.getId())==null || (get(parameter.getId()).equals(value)));

        return put(parameter.getId(),value);
    }

    public String get(String id) {
        Parameter p=Parameter.getByString(id);
        if(p==null) {
            throw new IllegalArgumentException("got unknown parameter id to map ("+id+")");
        }
        return get(p.getId());
    }

    public String get(Parameter p) {
        return get(p.getId());
    }

    public String get(int id) {
        return parameter.get(id);
    }

    protected void parse(ASN1Encodable ae) throws IOException{
        ASN1Sequence s1 = ASN1Sequence.getInstance(ae);
        for(ASN1Encodable o:s1) {
            ASN1TaggedObject to=ASN1TaggedObject.getInstance(o);
            Parameter p=Parameter.getById(to.getTagNo());
            if(p.isEncodable()) {
                parameter.put(to.getTagNo(),p.fromASN1Object(to.getObject()));
            } else {
                throw new IOException("unknown der tagged object when parsing parameter ("+to.getTagNo()+")");
            }
        }
    }

    @Override
    public String dumpValueNotation(String prefix,DumpType dumpType) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+ CRLF);
        int i=0;
        for(Map.Entry<Integer,String> e:parameter.entrySet()) {
            Parameter p=Parameter.getById(e.getKey());
            if(p!=null && p.isEncodable()) {
                if(i>0) {
                    sb.append( ","+CRLF);
                }
                sb.append(prefix + "  "+ p.toString()+" \""+e.getValue()+"\"");
                i++;
            }
        }
        sb.append(prefix).append(CRLF+prefix+"}");
        return sb.toString();
    }

    @Override
    public ASN1Object toASN1Object(DumpType dt) throws IOException {
        ASN1EncodableVector v =new ASN1EncodableVector();
        for(Map.Entry<Integer,String> e:parameter.entrySet()) {
            Parameter p=Parameter.getById(e.getKey());
            if(p!=null && p.isEncodable()) {
                v.add(new DERTaggedObject(p.getId(),p.toASN1Object(e.getValue())));
            }
        }
        return new DERSequence(v);
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        int i=0;
        for(Map.Entry<Integer,String> e:this.parameter.entrySet()) {
            if(i>0) {
                sb.append(", ");
            }
            sb.append(Parameter.getById(e.getKey()).toString()).append("=\"").append(e.getValue().toString()).append("\"");
            i++;
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(o==null) {
            return false;
        }
        if(! (o instanceof AlgorithmParameter)) {
            return false;
        }
        return ((AlgorithmParameter)(o)).compareTo(this)==0;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public int compareTo(AlgorithmParameter o) {
        return toString().compareTo(o.toString());
    }
}
