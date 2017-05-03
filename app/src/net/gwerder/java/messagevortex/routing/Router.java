package net.gwerder.java.messagevortex.routing;

import net.gwerder.java.messagevortex.accounting.Accountant;
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
     * Sets the accounting layer for the routing layer.
     *
     * @param accountant the accounting layer to be used
     * @return the previously set accounting layer
     */
    Accountant setAccountant(Accountant accountant);

    /***
     * Get the currently set accounting layer.
     *
     * @return the accounting layer or null if none
     */
    Accountant getAccountant();

}
