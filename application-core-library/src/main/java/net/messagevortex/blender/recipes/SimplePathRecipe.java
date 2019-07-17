package net.messagevortex.blender.recipes;

import java.util.List;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.RoutingBlock;

public class SimplePathRecipe extends BlenderRecipe {

  public SimplePathRecipe(String section) {
    // no parameters required
  }

  @Override
  public boolean isAppliable(List<IdentityStoreBlock> anonSet) {
    return (anonSet != null && anonSet.size() > 3);
  }

  @Override
  public RoutingBlock applyRecipe(List<IdentityStoreBlock> anonSet, IdentityStoreBlock from,
                                  IdentityStoreBlock to) {
    // creating a graph

    // selecting routes

    //selecting operations
    //building and returning routing block
    return null;
  }

}
