package net.messagevortex.router;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * <p>Extent with a rectangular shape.</p>
 */
public class TooltipExtentBox extends TooltipExtent {
  
  private final Rectangle2D box;

  /**
   * <p>Creates a new extent with a rectangular shape.</p>
   * @param box the box representing the shape
   */
  public TooltipExtentBox(Rectangle2D box) {
    this.box = (Rectangle2D) (box.clone());
  }

  @Override
  public boolean isInExtent(Point p) {
    return box.contains(p);
  }
  
}
