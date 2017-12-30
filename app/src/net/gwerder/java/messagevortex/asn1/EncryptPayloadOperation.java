package net.gwerder.java.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by martin.gwerder on 23.05.2017.
 */
public class EncryptPayloadOperation extends AbstractCryptPayloadOperation  implements Serializable {

    public static final long serialVersionUID = 100000000029L;

    EncryptPayloadOperation() {}

    public EncryptPayloadOperation(ASN1Encodable object) throws IOException {
        super(object);
    }

    @Override
    public Operation getNewInstance(ASN1Encodable object) throws IOException {
        return new EncryptPayloadOperation(object);
    }
}
