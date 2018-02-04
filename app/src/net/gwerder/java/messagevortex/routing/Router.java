package net.gwerder.java.messagevortex.routing;
// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import net.gwerder.java.messagevortex.accounting.Accountant;
import net.gwerder.java.messagevortex.blending.Blender;
import net.gwerder.java.messagevortex.blending.BlenderReceiver;

import java.util.List;

/**
 * Created by martin.gwerder on 03.04.2017.
 */
public interface Router extends BlenderReceiver {

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
