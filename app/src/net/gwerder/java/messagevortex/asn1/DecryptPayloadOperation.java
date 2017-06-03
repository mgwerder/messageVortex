package net.gwerder.java.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;

import java.io.IOException;

/**
 * Created by martin.gwerder on 23.05.2017.
 */
public class DecryptPayloadOperation extends AbstractCryptPayloadOperation {

    DecryptPayloadOperation() {}

    DecryptPayloadOperation(ASN1Encodable object) throws IOException {super(object);}

    @Override
    public Operation getNewInstance(ASN1Encodable object) throws IOException {
        return new DecryptPayloadOperation(object);
    }

}
