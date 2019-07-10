package net.messagevortex.blender.recipes;

import java.util.List;
import java.util.Vector;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.RoutingBlock;
import net.messagevortex.router.Edge;
import net.messagevortex.router.GraphSet;

public class LowAnonRecipe extends BlenderRecipe {

  long minStart = 10 * 1000;
  long maxStart = 20 * 1000;
  long minDelay = 20 * 1000;
  long maxDelay = 10 * 1000;

  public LowAnonRecipe(String section) {
    // no parameters required
  }

  @Override
  public boolean isAppliable(List<IdentityStoreBlock> anonSet) {
    return (anonSet != null && anonSet.size() > 4);
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
    GraphSet g = new GraphSet();
    IdentityStoreBlock fb = from;
    for (int i = 0; i < order.length; i++) {
      int ni = ExtendedSecureRandom.nextInt(order.length - i);
      for (int j = i - 1; j > 0; j--) {
        if (order[j] <= ni) {
          ni++;
        }
      }
      order[i] = ni;
      g.add(new Edge(fb, set.get(ni)
              , minStart + ExtendedSecureRandom.nextInt((int) (maxStart - minStart))
              , minDelay + ExtendedSecureRandom.nextInt((int) (maxDelay - minDelay))));
      fb = set.get(ni);
    }


    // split message at start


    // send packages along the line

    // add some dead ends

    return null;
  }

}
