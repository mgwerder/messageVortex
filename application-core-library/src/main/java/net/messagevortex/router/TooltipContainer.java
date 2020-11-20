package net.messagevortex.router;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class TooltipContainer {

  private Map<TooltipExtent,String> tips = new HashMap<>();

  public void addTooltip(TooltipExtent extent, String text) {
    tips.put(extent,text);
  }

  public String getTooltipText(Point p) {
    for(Map.Entry<TooltipExtent,String> e:tips.entrySet()) {
      if(e.getKey().isInExtent(p)) {
        return e.getValue();
      }
    }
    return null;
  }

}
