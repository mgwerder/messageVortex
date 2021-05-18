package net.messagevortex.router;

import java.awt.*;

/**
 * <p>Abstract class representing any tooltip extent.</p>
 */
public abstract class TooltipExtent {

  /**
   * <p>Returns true if the given point is within the extent.</p>
   *
   * @param p the point to be tested
   * @return true if the point lies within the extent
   */
  public abstract boolean isInExtent(Point p);
  
}
