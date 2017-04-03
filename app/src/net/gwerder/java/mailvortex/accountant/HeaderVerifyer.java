package net.gwerder.java.mailvortex.accountant;

import net.gwerder.java.mailvortex.asn1.Identity;

/**
 * Created by martin.gwerder on 03.04.2017.
 */
public interface HeaderVerifyer {

    public boolean verfyHeaderForProcessing(Identity header);

}
