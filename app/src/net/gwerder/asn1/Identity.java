package net.gwerder.asn1;

import org.bouncycastle.asn1.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;

public class Identity extends Block {

    protected AsymetricKey identityKey = null;
    protected long serial;
    protected int maxReplays;
    protected UsagePeriod valid = null;
    protected int[] forwardSecret = null;
    protected byte[] decryptionKeyRaw =new byte[0];
    protected SymetricKey decryptionKey;
    protected Request[] requests;
    protected String padding=null;

    public Identity() throws NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException,IOException,NoSuchPaddingException,InvalidKeyException,IllegalBlockSizeException,BadPaddingException,NoSuchProviderException,InvalidKeySpecException {
        identityKey=new AsymetricKey(Algorithm.RSA,1024);
        serial = (long)(Math.random()*4294967295L);
        maxReplays=1;
        valid=new UsagePeriod(3600);
        decryptionKey=new SymetricKey(Algorithm.AES256);
        decryptionKeyRaw=identityKey.encrypt(decryptionKey.encodeDER().toASN1Primitive().getEncoded(),false);
        requests=new Request[0];
    }

    public Identity(ASN1Encodable to) throws ParseException,IOException,NoSuchAlgorithmException  {
        parse(to);
    }

    @Override
    protected void parse(ASN1Encodable to) throws ParseException,IOException,NoSuchAlgorithmException {
        ASN1Sequence s1 = ASN1Sequence.getInstance(to);
        int i=0;
        identityKey=new AsymetricKey(s1.getObjectAt(i++));
        serial = ((ASN1Integer)(s1.getObjectAt(i++))).getValue().intValue();
        maxReplays = ((ASN1Integer)(s1.getObjectAt(i++))).getValue().intValue();
        valid=new UsagePeriod(s1.getObjectAt(i++));
        try {
            ASN1Sequence s2 = ((ASN1Sequence)(s1.getObjectAt(i++)));
            forwardSecret = new int[s2.size()];
            for(int y=0;y<s2.size();y++) {
                forwardSecret[y] = ((ASN1Integer)(s2.getObjectAt(y))).getValue().intValue();
            }
        } catch(Exception e) {
            // redo this line if not optional forwardSecret
            i--;
            // FIXME remove below
            // e.printStackTrace();
        }
        decryptionKey=new SymetricKey(((ASN1OctetString)(s1.getObjectAt(i++))).getEncoded(),identityKey,true);
        ASN1Sequence s2=((ASN1Sequence)(s1.getObjectAt(i++)));
        requests= new Request[s2.size()];
        for(int y=0;y<s2.size();y++) {
            requests[y]=new Request(s2.getObjectAt(y));
        }
        if(s1.size()>i) {
            padding=((ASN1String)(s1.getObjectAt(i))).getString();
        }
    }

    public String dumpValueNotation(String prefix) throws IOException {
        StringBuilder sb=new StringBuilder();
        sb.append("{"+CRLF);
        sb.append(prefix+"  identityKey "+identityKey.dumpValueNotation(prefix+"  ", AsymetricKey.DumpType.PRIVATE_COMMENTED)+","+CRLF);
        sb.append(prefix+"  serial "+serial+","+CRLF);
        sb.append(prefix+"  maxReplays "+maxReplays+","+CRLF);
        sb.append(prefix+"  valid "+valid.dumpValueNotation(prefix+"  ")+","+CRLF);
        if(forwardSecret!=null) {
            sb.append(prefix + "  forwardSecret { "+CRLF);
            for (int i = 0; i < forwardSecret.length; i++) {
                sb.append(forwardSecret[i] + (i < forwardSecret.length - 1 ? "," : "")+CRLF);
            }
            sb.append(" },"+CRLF);
        }
        ASN1Encodable a=decryptionKey.encodeDER();
        if(a==null) {
            sb.append(prefix + "  decryptionKey ''B,"+CRLF);
        } else {
            sb.append(prefix + "  decryptionKey "+toHex(a.toASN1Primitive().getEncoded())+","+CRLF);
        }
        sb.append(prefix+"  requests {"+CRLF);
        for (int i = 0; i < requests.length; i++) {
            sb.append(valid.dumpValueNotation(prefix + "  ")+CRLF);
            sb.append(requests[i].dumpValueNotation(prefix + "  ")+CRLF);
        }
        sb.append(prefix+"  }");
        if(padding!=null) {
            sb.append(","+CRLF);
            sb.append(prefix+"  padding "+toHex(padding.getBytes())+CRLF);
        } else sb.append(CRLF);
        sb.append(prefix+"}");
        return sb.toString();
    }

    @Override
    public ASN1Encodable encodeDER() {
        // FIXME
        return null;
    }
}
