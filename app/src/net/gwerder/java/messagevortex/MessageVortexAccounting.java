package net.gwerder.java.messagevortex;

import net.gwerder.java.messagevortex.accounting.Accountant;

/**
 * Created by Martin on 01.02.2018.
 */
public class MessageVortexAccounting {

    private Accountant accountant = null;

    public MessageVortexAccounting( Accountant accountant ) {
        this.accountant = accountant;
    }

    public final Accountant getAccountant() {
        return accountant;
    }

    public final Accountant setAccountant( Accountant accountant) {
        Accountant ret = this.accountant;
        this.accountant = accountant;
        return ret;
    }

}
