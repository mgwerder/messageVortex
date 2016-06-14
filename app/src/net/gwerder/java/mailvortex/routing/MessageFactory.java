package net.gwerder.java.mailvortex.routing;

import net.gwerder.java.mailvortex.asn1.IdentityStore;
import net.gwerder.java.mailvortex.asn1.IdentityStoreBlock;
import net.gwerder.java.mailvortex.asn1.Message;

import java.security.SecureRandom;

/**
 * Factory class to build full message (anonymizing structure)
 * <p>
 * Created by martin.gwerder on 06.06.2016.
 */
public abstract class MessageFactory {

    protected static final SecureRandom sr = new SecureRandom();

    protected Message fullmsg = null;

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
        fullmsg.hotspot = anonGroupMembers[sr.nextInt( anonGroupMembers.length )];

        fullmsg.build();

        return fullmsg;
    }

    private Message getMessage() {
        return this.fullmsg;
    }

    public abstract void build();

}
