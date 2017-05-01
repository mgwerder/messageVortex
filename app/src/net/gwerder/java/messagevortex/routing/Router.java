package net.gwerder.java.messagevortex.routing;

import net.gwerder.java.messagevortex.accountant.Accountant;
import net.gwerder.java.messagevortex.blending.Blender;
import net.gwerder.java.messagevortex.blending.BlenderListener;

import java.util.List;

/**
 * Created by martin.gwerder on 03.04.2017.
 */
public interface Router extends BlenderListener {

    /***
     * Adds a blending layer to the routing infrastructure.
     *
     * @param blendingLayer the blending layer to be added
     * @return true if the blending layer has been accepted
     *         (only one blending layer per transport scheme is accepted)
     */
    boolean addBlendingLayer(Blender blendingLayer);

    /***
     * Removes a previously added blending layer.
     *
     * @param blendingLayer the blending layer to be removed
     * @return true if the blending layer was found (and removed)
     */
    boolean removeBlendingLayer(Blender blendingLayer);

    /***
     * Get a list of all currently known blending layers.
     *
     * @return a list of a registered blending layers
     */
    List<Blender> getAllBlendingLayer();

    /***
     * Sets the accountant layer for the routing layer.
     *
     * @param accountant the accountant layer to be used
     * @return the previously set accountant layer
     */
    Accountant setAccountant(Accountant accountant);

    /***
     * Get the currently set accountant layer.
     *
     * @return the accountant layer or null if none
     */
    Accountant getAccountant();

}
