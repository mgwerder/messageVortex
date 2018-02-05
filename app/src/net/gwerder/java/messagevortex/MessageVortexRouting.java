package net.gwerder.java.messagevortex;

import net.gwerder.java.messagevortex.accounting.Accountant;
import net.gwerder.java.messagevortex.asn1.VortexMessage;
import net.gwerder.java.messagevortex.blending.BlenderReceiver;
import net.gwerder.java.messagevortex.routing.operation.RoutingSender;

/**
 * Created by Martin on 01.02.2018.
 */
public class MessageVortexRouting implements BlenderReceiver {

    private RoutingSender routingSender;
    private Accountant accountant;

    public MessageVortexRouting( Accountant accountant, RoutingSender routingSender ) {
        setRoutingSender( routingSender );
        setAccountant( accountant );
    }

    public final RoutingSender getRoutingSender() {
        return routingSender;
    }

    public final RoutingSender setRoutingSender( RoutingSender routingSender ) {
        RoutingSender ret = routingSender;
        this.routingSender = routingSender;
        return ret;
    }

    public final Accountant getAccountant() {
        return accountant;
    }

    public final Accountant setAccountant( Accountant accountant ) {
        Accountant ret = this.accountant;
        this.accountant = accountant;
        return ret;
    }

    @Override
    public boolean gotMessage( VortexMessage message ) {

        return false;
    }
}
