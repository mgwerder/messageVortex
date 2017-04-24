package net.gwerder.java.messagevortex.blending;

import net.gwerder.java.messagevortex.accountant.HeaderVerifyer;
import net.gwerder.java.messagevortex.asn1.BlendingSpec;
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
     * @param verifier The verifier to be used
     * @return previous/old verifier
     */
    public HeaderVerifyer setHeaderVerifyer(HeaderVerifyer verifier);

    /***
     * This method is called by the routing layer to blend a message.
     *
     * @param message the message to be blended
     * @return true if blended successfully and sent by the transport layer
     */
    public boolean blendMessage(BlendingSpec target,Message message);

    /***
     * Returns the address supported for blending.
     *
     * The address is specified by &lt;transport&gt;&lt;address&gt;!&lt;publickey&gt;.
     *
     * @return The vortex adress.
     */
    public String getBlendingAddress();

}
