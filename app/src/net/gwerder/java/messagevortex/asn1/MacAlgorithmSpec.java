package net.gwerder.java.messagevortex.asn1;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by martin.gwerder on 29.12.2017.
 */
public class MacAlgorithmSpec extends SymmetricAlgorithmSpec implements Serializable {

    public static final long serialVersionUID = 100000000011L;

    public MacAlgorithmSpec(AsymmetricAlgorithmSpec to) throws IOException {
        super(to);
    }

}
