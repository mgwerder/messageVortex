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
import net.gwerder.java.messagevortex.asn1.encryption.DumpType;
import org.bouncycastle.asn1.*;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by martin.gwerder on 14.04.2016.
 */
public class RoutingBlock extends AbstractBlock  implements Serializable {

    public static final long serialVersionUID = 100000000014L;

    public static final int MURB = 131;
    public static final int OPERATIONS = 132;

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }

    private static AsymmetricKey dummyKey;
    static{
        try {
            dummyKey = new AsymmetricKey();
        } catch(IOException ioe) {
            LOGGER.log(Level.SEVERE,"error while getting logging key for dummy blocks",ioe);
        }
    }

    public static final int PREFIX_PLAIN     =331;
    public static final int PREFIX_ENCRYPTED =332;
    public static final int ROUTING_PLAIN     =333;
    public static final int ROUTING_ENCRYPTED =334;

    private byte[] encrypted =null;

    private BlendingSpec    recipient;
    private long            minProcessTime =0;
    private long            maxProcessTime =0;
    private PrefixBlock[]   prefix;
    private RoutingBlock[]  nextHop;
    private long            forwardSecret=-1;
    private RoutingBlock    murbReplyBlock = null;
    private long            murbMaxReplay =-1;
    private UsagePeriod     murbValidity  = null;
    private List<Operation> operation = new ArrayList<>();
    private AssemblyBlock[] assembly=null;

    public RoutingBlock() {
        recipient = new BlendingSpec("");
        prefix    = new PrefixBlock[0];
        nextHop   = new RoutingBlock[0];
    }

    public RoutingBlock(byte[] b,boolean encrypted) throws IOException {
        if(encrypted) {
            this.encrypted= Arrays.copyOf(b,b.length);
        } else {
            parse(b);
        }
    }

    public RoutingBlock(ASN1Encodable to) throws IOException {
        parse(to);
    }

    private PrefixBlock[] getPrefix(ASN1Primitive seq) throws IOException {
        // if it is encrypted we have no decryption key for it anyway
        List<PrefixBlock> ap = new ArrayList<>();
        for (ASN1Encodable e : ASN1Sequence.getInstance(seq)) {
            ap.add(new PrefixBlock(e.toASN1Primitive(), null));
        }
        return ap.toArray(new PrefixBlock[ap.size()]);
    }

    @Override
    protected void parse(ASN1Encodable to) throws IOException {
        Operation.init();
        if(isEncrypted()) {
            throw new IOException("Unable to encode to asn encrypted stream");
        }

        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        int i=0;

        // get recipient
        recipient=new BlendingSpec(s1.getObjectAt(i++));

        // get times
        minProcessTime=ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();
        maxProcessTime=ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().intValue();

        // get prefix block
        // FIXME check thorougly. How is this tag preserved when reencoding
        ASN1TaggedObject ae=ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
        switch(ae.getTagNo()) {
            case PREFIX_PLAIN:
                LOGGER.log(Level.INFO,"parsing plain prefix");
                prefix=getPrefix(ae.getObject().toASN1Primitive());
                break;
            case PREFIX_ENCRYPTED:
                LOGGER.log(Level.INFO,"parsing encrypted prefix");
                prefix=getPrefix(ae.getObject().toASN1Primitive());
                break;
            default:
                throw new IOException( "Error parsing prefix (expected: "+PREFIX_PLAIN+" or "+PREFIX_ENCRYPTED+";got:"+ae.getTagNo()+")");
        }
        if(prefix==null) {
            throw new NullPointerException("prefix should not parse to null (decoding)");
        }

        // parse nextHop
        ae=ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
        switch(ae.getTagNo()) {
            case ROUTING_PLAIN:
            case ROUTING_ENCRYPTED:
                // if it is encrypted we have no decryption key for it anyway
                List<RoutingBlock> p2=new ArrayList<>();
                for (ASN1Encodable b :ASN1Sequence.getInstance( ae.getObject() ) ) {
                    p2.add(new RoutingBlock(b));
                }
                if(p2.size()!=prefix.length) {
                    throw new IOException( "missmatch in length of prefix and routing block");
                } else {
                    nextHop=p2.toArray(new RoutingBlock[p2.size()]);
                }
                break;
            default:
                throw new IOException( "Error parsing prefix (expected: "+ROUTING_PLAIN+" or "+ROUTING_ENCRYPTED+";got:"+ae.getTagNo()+")");
        }

        // parse forward secret
        forwardSecret=ASN1Integer.getInstance(s1.getObjectAt(i++)).getValue().longValue();

        // parse reply block
        ae = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
        if (ae.getTagNo() == MURB) {
            ASN1Sequence s2=ASN1Sequence.getInstance(ae.getObject());
            if(s2.size()!=3) {
                throw new IOException("invalid sequence size for reply block (got: "+s2.size()+"; expected: 3)");
            }
            murbReplyBlock=new RoutingBlock(s2.getObjectAt(0));
            murbMaxReplay=ASN1Integer.getInstance(s2.getObjectAt(1)).getPositiveValue().intValue();
            murbValidity=new UsagePeriod(s2.getObjectAt(2));
        } else if(ae.getTagNo()==OPERATIONS) {
            i--;
        } else {
            throw new IOException("Got unknown tag number (got: "+ae.getTagNo()+"; expected: "+MURB+" or "+OPERATIONS+")");
        }

        // parse operations
        ae = ASN1TaggedObject.getInstance(s1.getObjectAt(i++));
        if (ae.getTagNo() == OPERATIONS) {
            ASN1Sequence s2=ASN1Sequence.getInstance(ae.getObject());
            List<Operation> o=new ArrayList<>();
            if(s2.size()>0) {
                for (ASN1Encodable obj : s2) {
                    Operation op=Operation.getInstance(obj);
                    o.add(op);
                }
                operation.clear();
                operation.addAll(o);
            }
        } else {
            throw new IOException("Got unknown tag number (got: "+ae.getTagNo()+"; expected: "+OPERATIONS+")");
        }

        // parse assembly
        ASN1Sequence s2=ASN1Sequence.getInstance(s1.getObjectAt(i++));
        List<AssemblyBlock> o=new ArrayList<>();
        for(ASN1Encodable obj:s2) {
            o.add(new AssemblyBlock(obj));
        }
    }

    public boolean isEncrypted() {
        return this.encrypted!=null;
    }

    public long getFirstProcessTime() {
        return minProcessTime;
    }

    public long setFistProcessTime(long minProcessTime) {
        long old = minProcessTime;
        encrypted=null;
        this.minProcessTime=minProcessTime;
        return old;
    }

    public long getLastProcessTime() {
        return maxProcessTime;
    }

    public long setLastProcessTime(long maxProcessTime) {
        long old = maxProcessTime;
        encrypted=null;
        this.maxProcessTime=maxProcessTime;
        return old;
    }

    @Override
    public ASN1Object toASN1Object(DumpType dumpType) throws IOException{
        if(prefix==null) {
            throw new NullPointerException("prefix may not be null when encoding");
        }
        ASN1EncodableVector v=new ASN1EncodableVector();

        // add recipient
        v.add(recipient.toASN1Object(dumpType));

        v.add(new ASN1Integer(minProcessTime));
        v.add(new ASN1Integer(maxProcessTime));

        // add prefix
        switch(dumpType) {
            case ALL_UNENCRYPTED:
                ASN1EncodableVector v2=new ASN1EncodableVector();
                for(PrefixBlock p:prefix) {
                    v2.add(p.toASN1Object(dumpType));
                }
                v.add(new DERTaggedObject(PREFIX_PLAIN,new DERSequence(v2)));
                break;
            case PRIVATE_COMMENTED:
            case PUBLIC_ONLY:
            case ALL:
                ASN1EncodableVector v3=new ASN1EncodableVector();
                for(PrefixBlock p:prefix) {
                    v3.add(new DEROctetString(p.toEncBytes()));
                }
                v.add(new DERTaggedObject(PREFIX_ENCRYPTED,new DERSequence(v3)));
                break;
            default:
                throw new IOException( "Error encoding prefix (unknown dump type "+dumpType.name()+")");
        }

        // add nextHop
        switch(dumpType) {
            case ALL_UNENCRYPTED:
                ASN1EncodableVector v2=new ASN1EncodableVector();
                for(RoutingBlock p:nextHop) {
                    v2.add(p.toASN1Object(dumpType));
                }
                v.add(new DERTaggedObject(ROUTING_PLAIN,new DERSequence(v2)));
                break;
            case PRIVATE_COMMENTED:
            case PUBLIC_ONLY:
            case ALL:
                ASN1EncodableVector v3=new ASN1EncodableVector();
                for(RoutingBlock p:nextHop) {
                    v3.add(new DEROctetString(p.toEncBytes()));
                }
                v.add(new DERTaggedObject(ROUTING_ENCRYPTED,new DERSequence(v3)));
                break;
            default:
                throw new IOException( "Error encoding prefix (unknown dump type "+dumpType.name()+")");
        }

        v.add(new ASN1Integer(forwardSecret));

        if(murbMaxReplay>0) {
            ASN1EncodableVector v2=new ASN1EncodableVector();
            v2.add(murbReplyBlock.toASN1Object(dumpType));
            v2.add(new ASN1Integer(murbMaxReplay));
            v2.add(murbValidity.toASN1Object(dumpType));
            v.add(new DERTaggedObject(MURB,new DERSequence(v2)));
        }

        ASN1EncodableVector v2=new ASN1EncodableVector();
        if(operation!=null && !operation.isEmpty()) {
            for (Operation o : operation) {
                v2.add(o.toASN1Object(dumpType));
            }
        }
        v.add(new DERTaggedObject(OPERATIONS,new DERSequence(v2)));

        v2=new ASN1EncodableVector();
        if(assembly!=null && assembly.length>0) {
            for (AssemblyBlock a : assembly) {
                v2.add(a.toASN1Object(dumpType));
            }
        }
        v.add(new DERSequence(v2));

        return new DERSequence(v);
    }

    public boolean addOperation(Operation o) {
        return operation.add(o);
    }

    public byte[] toEncBytes() {
        return encrypted;
    }

    @Override
    public String dumpValueNotation(String prefix,DumpType dumpType) {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        sb.append(prefix+"  "+CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if( o.getClass() != this.getClass() ) {
            return false;
        }
        RoutingBlock rb=(RoutingBlock)o;
        try {
            return Arrays.equals(rb.toBytes(DumpType.ALL_UNENCRYPTED), toBytes(DumpType.ALL_UNENCRYPTED));
        } catch(IOException ioe) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return prepareDump(dumpValueNotation("",DumpType.ALL_UNENCRYPTED)).hashCode();
    }

}
