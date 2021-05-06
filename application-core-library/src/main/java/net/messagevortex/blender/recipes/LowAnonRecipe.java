package net.messagevortex.blender.recipes;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.asn1.EncryptPayloadOperation;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.RoutingCombo;
import net.messagevortex.router.Edge;
import net.messagevortex.router.GraphSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LowAnonRecipe extends BlenderRecipe {

  long minStart = 10 * 1000;
  long maxStart = 20 * 1000;
  long minDelay = 20 * 1000;
  long maxDelay = 10 * 1000;

  /***
   * <p>Constructor to create low anon recipe.</p>
   *
   * <p>This class is required for bootstrapping a new nenber not having sufficient ephemeral
   * identities to use a high anonymity recipe.</p>
   *
   * @param section the name of the configuration section to be used
   */
  public LowAnonRecipe(String section) {
    // no parameters required
  }

  /***
   * <p>Checks if the recipe is applicaable.</p>
   *
   * <p>Recipe is applicable if less than four identities are available. For larger anonymisation
   * sets a more secure recipe is assumed to be available.</p>
   *
   * @param anonSet the anonymisation set available
   * @return true if recipe may be applied
   */
  @Override
  public boolean isAppliable(Set<IdentityStoreBlock> anonSet) {
    return (anonSet != null && anonSet.size() <= 4);
  }

  @Override
  public RoutingCombo applyRecipe(Set<IdentityStoreBlock> anonSet, IdentityStoreBlock from,
                                  IdentityStoreBlock to) throws IOException {
    // select random order
    List<IdentityStoreBlock> set = new ArrayList<>();
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
      // set timings between nodes
      g.add(new Edge(fb, set.get(ni),
              minStart + ExtendedSecureRandom.nextInt((int) (maxStart - minStart)),
              minDelay + ExtendedSecureRandom.nextInt((int) (maxDelay - minDelay))));
      fb = set.get(ni);
    }

    // encrypt and split message at start
    RoutingCombo rb = new RoutingCombo();
    int targetBlock = ExtendedSecureRandom.nextInt(1024, 65737);
    rb.addOperation(new EncryptPayloadOperation(0, targetBlock, null));


    // send packages along the line
    // FIXME

    // add some dead ends
    // FIXME

    return null;
  }

}
