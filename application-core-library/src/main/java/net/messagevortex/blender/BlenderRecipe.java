package net.messagevortex.blender;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.RoutingBlock;

public abstract class BlenderRecipe {

  static Set<BlenderRecipe> recipes = new ConcurrentSkipListSet<>();

  static SecureRandom esr = ExtendedSecureRandom.getSecureRandom();

  static BlenderRecipe getRecipe(List<IdentityStoreBlock> anonSet) {
    List<BlenderRecipe> l = new ArrayList<>();
    for (BlenderRecipe r : recipes) {
      if (r.isAppliable(anonSet)) {
        l.add(r);
      }
    }
    return l.get(esr.nextInt(l.size()));
  }

  static void clearRecipes() {
    recipes.clear();
  }

  static void addRecipe(BlenderRecipe add) {
    recipes.add(add);
  }

  abstract boolean isAppliable(List<IdentityStoreBlock> anonSet);

  abstract RoutingBlock applyRecipe(List<IdentityStoreBlock> anonSet,IdentityStoreBlock from,IdentityStoreBlock to);

}
