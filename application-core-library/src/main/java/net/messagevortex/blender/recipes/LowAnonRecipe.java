package net.messagevortex.blender.recipes;

import java.util.List;
import java.util.Vector;
import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.RoutingBlock;

public class LowAnonRecipe extends BlenderRecipe {

  public LowAnonRecipe(String section) {
    // no parameters required
  }

  @Override
  public boolean isAppliable(List<IdentityStoreBlock> anonSet) {
    if (anonSet != null && anonSet.size() > 0 && anonSet.size() > 4) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public RoutingBlock applyRecipe(List<IdentityStoreBlock> anonSet, IdentityStoreBlock from,
                                  IdentityStoreBlock to) {
    // select random order
    List<IdentityStoreBlock> set = new Vector<>();
    //set.add(from);
    set.add(to);
    set.addAll(anonSet);

    // select random order
    int[] order = new int[set.size()];
    for (int i = 0; i < order.length; i++) {
      int ni = ExtendedSecureRandom.nextInt(order.length - i);
      for (int j = i-1; j > 0; j--) {
        if (order[j] <= ni ) {
          ni ++;
        }
      }
      order[i] = ni;
    }


    // split message at start

    // send packages along the line

    // add some dead ends

    return null;
  }

}
