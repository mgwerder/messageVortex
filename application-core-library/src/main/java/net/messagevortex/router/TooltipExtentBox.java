package net.messagevortex.router;

import java.awt.Point;
import java.awt.geom.Rectangle2D;

public class TooltipExtentBox extends TooltipExtent {
  
  private final Rectangle2D box;
  
  public TooltipExtentBox(Rectangle2D box) {
    this.box = (Rectangle2D) (box.clone());
  }
  
  public boolean isInExtent(Point p) {
    return box.contains(p);
  }
  
}
