package net.messagevortex.router;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * <p>Extent with a circular shape.</p>
 */
public class TooltipExtentCircular extends TooltipExtent {
  
  private final Ellipse2D circle;

  /**
   * <p>Creates an extent with a circular shape.</p>
   *
   * @param circle the circle representing the shape
   */
  public TooltipExtentCircular(Ellipse2D circle) {
    this.circle = (Ellipse2D) (circle.clone());
  }

  @Override
  public boolean isInExtent(Point p) {
    return circle.contains(p);
  }
  
}
