package net.messagevortex.router;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class TooltipContainer {
  
  private final Map<TooltipExtent, String> tips = new HashMap<>();
  
  /**
   * <p>Adds a tooltip at the given position.</p>
   *
   * @param extent the area in which the tooltip should be shown
   * @param text   the text to be shown
   */
  public void addTooltip(TooltipExtent extent, String text) {
    tips.put(extent, text);
  }
  
  /**
   * <p>Returns the first tooltip text found for a given position.</p>
   *
   * @param p position for the tooltip
   * @return the text or null if no tooltip is specified
   */
  public String getTooltipText(Point p) {
    for (Map.Entry<TooltipExtent, String> e : tips.entrySet()) {
      if (e.getKey().isInExtent(p)) {
        return e.getValue();
      }
    }
    return null;
  }
  
}
