package net.messagevortex.blender;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.RoutingBlock;

public abstract class BlenderRecipe {

  static Map<String, Set<BlenderRecipe>> recipes = new HashMap<>();

  static SecureRandom esr = ExtendedSecureRandom.getSecureRandom();

  private static final String DEFAULT = "default";

  static BlenderRecipe getRecipe(String identifier, List<IdentityStoreBlock> anonSet)
          throws IOException {
    if (identifier == null) {
      identifier = DEFAULT;
    }
    if (recipes.get(identifier) == null) {
      return null;
    }
    List<BlenderRecipe> l = new ArrayList<>();
    for (BlenderRecipe r : recipes.get(identifier)) {
      if (r.isAppliable(anonSet)) {
        l.add(r);
      }
    }
    if (l.size() == 0) {
      throw new IOException("No candidates found for the given anon set");
    }
    return l.get(esr.nextInt(l.size()));
  }

  static void clearRecipes(String identifier) {
    if (identifier == null) {
      identifier = DEFAULT;
    }
    synchronized (recipes) {
      if (recipes.get(identifier) != null) {
        recipes.get(identifier).clear();
      }
    }
  }

  static void addRecipe(String identifier, BlenderRecipe add) {
    if (identifier == null) {
      identifier = DEFAULT;
    }
    synchronized (recipes) {
      if (recipes.get(identifier) == null) {
        recipes.put(identifier, new ConcurrentSkipListSet<>());
      }
      recipes.get(identifier).add(add);
    }
  }

  abstract boolean isAppliable(List<IdentityStoreBlock> anonSet);

  abstract RoutingBlock applyRecipe(List<IdentityStoreBlock> anonSet, IdentityStoreBlock from,
                                    IdentityStoreBlock to);

}
