package net.messagevortex.router;

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
