package net.messagevortex.router;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class TooltipExtentCircular extends TooltipExtent {
  
  private Ellipse2D circle;
  
  public TooltipExtentCircular(Ellipse2D circle) {
    this.circle = (Ellipse2D) (circle.clone());
  }
  
  public boolean isInExtent(Point p) {
    return circle.contains(p);
  }
  
}