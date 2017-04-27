package net.gwerder.java.messagevortex.routing;

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import net.gwerder.java.messagevortex.asn1.IdentityStore;
import net.gwerder.java.messagevortex.asn1.IdentityStoreBlock;
import net.gwerder.java.messagevortex.asn1.VortexMessage;

/**
 * Factory class to build full message (anonymizing structure)
 * <p>
 * Created by martin.gwerder on 06.06.2016.
 */
public abstract class MessageFactory {

    protected static final ExtendedSecureRandom esr = new ExtendedSecureRandom();

    protected VortexMessage fullmsg = null;

    protected String msg = "";
    protected IdentityStoreBlock source = null;
    protected IdentityStoreBlock target = null;
    protected IdentityStoreBlock hotspot = null;
    protected IdentityStore identityStore = null;

    protected MessageFactory() {

    }

    public static MessageFactory buildMessage(String msg, int source, int target, IdentityStoreBlock[] anonGroupMembers, IdentityStore is) {
        MessageFactory fullmsg = new SimpleMessageFactory( msg, source, target, anonGroupMembers, is );

        // selecting hotspot
        fullmsg.hotspot = anonGroupMembers[esr.nextInt( anonGroupMembers.length )];

        fullmsg.build();

        return fullmsg;
    }

    private VortexMessage getMessage() {
        return this.fullmsg;
    }

    public abstract void build();

    public abstract GraphSet getGraph();

}
