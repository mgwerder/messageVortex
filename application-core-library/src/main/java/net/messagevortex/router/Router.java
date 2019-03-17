package net.messagevortex.router;

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

import net.messagevortex.RunningDaemon;
import net.messagevortex.accounting.Accountant;
import net.messagevortex.blender.Blender;
import net.messagevortex.blender.BlendingReceiver;

import java.util.List;

public interface Router extends BlendingReceiver, RunningDaemon {

  /***
   * <p>Adds a blender layer to the router infrastructure.</p>
   *
   * @param blendingLayer the blender layer to be added
   * @return true if the blender layer has been accepted
   *         (only one blender layer per transport scheme is accepted)
   */
  boolean addBlendingLayer(Blender blendingLayer);

  /***
   * <p>Removes a previously added blender layer.</p>
   *
   * @param blendingLayer the blender layer to be removed
   * @return true if the blender layer was found (and removed)
   */
  boolean removeBlendingLayer(Blender blendingLayer);

  /***
   * <p>Get a list of all currently known blender layers.</p>
   *
   * @return a list of a registered blender layers
   */
  List<Blender> getAllBlendingLayer();

  /***
   * <p>Sets the accounting layer for the router layer.</p>
   *
   * @param accountant the accounting layer to be used
   * @return the previously set accounting layer
   */
  Accountant setAccountant(Accountant accountant);

  /***
   * <p>Get the currently set accounting layer.</p>
   *
   * @return the accounting layer or null if none
   */
  Accountant getAccountant();

}