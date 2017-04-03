package net.gwerder.java.messagevortex.blending;

import net.gwerder.java.messagevortex.accountant.HeaderVerifyer;
import net.gwerder.java.messagevortex.asn1.Message;
import net.gwerder.java.messagevortex.routing.IncomingMessageRouterListener;

/**
 * Interface specifying a blender.
 */
public interface Blender {

    /***
     * Sets the listening routing layer.
     *
     * All future messages sucessfully extracted and authorized by the header verifyer are passed to this object.
     *
     * @param listener The listening routing layer
     * @return The old/previous routing layer
     */
    public IncomingMessageRouterListener setIncomingMessageListener(IncomingMessageRouterListener listener);

    /***
     * Sets the verifier which is called after decoding the header block.
     *
     * All decoded headers will be verified using this verifier.
     *
     * @param verifyer
     * @return
     */
    public HeaderVerifyer setHeaderVerifyer(HeaderVerifyer verifyer);

    /***
     * This method is called by the routing layer to blend a message.
     *
     * @// FIXME: 03.04.2017 blending spec missing in call
     *
     * @param message the message to be blended
     * @return true if blended successfully and sent by the transport layer
     */
    public boolean blendMessage(Message message);

    /***
     * Returns the address supported for blending.
     *
     * The address is specified by &lt;transport&gt;&lt;address&gt;!&lt;publickey&gt;.
     *
     * @return The vortex adress.
     */
    public String getBlendingAddress();

}
