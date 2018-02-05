package net.gwerder.java.messagevortex.asn1;

import org.bouncycastle.asn1.ASN1Encodable;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by martin.gwerder on 29.12.2017.
 */
public class SymmetricAlgorithmSpec  extends AsymmetricAlgorithmSpec implements Serializable {

    public static final long serialVersionUID = 100000000016L;

    public SymmetricAlgorithmSpec(AsymmetricAlgorithmSpec to) throws IOException {
        super(to);
    }

    public SymmetricAlgorithmSpec(ASN1Encodable to) throws IOException {
        super(to);
    }

}
