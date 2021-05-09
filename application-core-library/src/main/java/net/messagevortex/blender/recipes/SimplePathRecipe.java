package net.messagevortex.blender.recipes;

import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.RoutingCombo;

import java.util.Set;

public class SimplePathRecipe extends BlenderRecipe {

  public SimplePathRecipe(String section) {
    // no parameters required
  }

  @Override
  public boolean isAppliable(Set<IdentityStoreBlock> anonSet) {
    return (anonSet != null && anonSet.size() > 3);
  }

  @Override
  public RoutingCombo applyRecipe(Set<IdentityStoreBlock> anonSet, IdentityStoreBlock from,
                                  IdentityStoreBlock to) {
    // creating a graph

    // selecting routes

    //selecting operations

    //building and returning routing block
    return null;
  }

}
