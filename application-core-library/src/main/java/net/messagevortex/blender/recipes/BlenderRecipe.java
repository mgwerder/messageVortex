package net.messagevortex.blender.recipes;

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

public abstract class BlenderRecipe implements Comparable<BlenderRecipe> {

  static Map<String, Set<BlenderRecipe>> recipes = new HashMap<>();

  private static SecureRandom esr = ExtendedSecureRandom.getSecureRandom();

  private static final String DEFAULT = "default";

  /***
   * <p>Get a recipe from the specified recipe set.</p>
   *
   * @param identifier the name of the recipe set
   * @param anonSet    the anonymity set to be used
   * @return           a random recipe
   *
   * @throws IOException if no candidates can be found
   */
  public static BlenderRecipe getRecipe(String identifier, List<IdentityStoreBlock> anonSet)
          throws IOException {
    if (identifier == null) {
      identifier = DEFAULT;
    }

    if (recipes.get(identifier) == null || recipes.get(identifier).size() == 0) {
      throw new IOException("Set of recipes is empty or does not exist");
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

  public static void clearRecipes(String identifier) {
    if (identifier == null) {
      identifier = DEFAULT;
    }

    synchronized (recipes) {
      if (recipes.get(identifier) != null) {
        recipes.get(identifier).clear();
      }
    }
  }

  public static void addRecipe(String identifier, BlenderRecipe add) {
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

  public abstract boolean isAppliable(List<IdentityStoreBlock> anonSet);

  public abstract RoutingBlock applyRecipe(List<IdentityStoreBlock> anonSet,
                                           IdentityStoreBlock from,
                                           IdentityStoreBlock to);

  @Override
  public int compareTo(BlenderRecipe o) {
    return (""+hashCode()).compareTo(""+o.hashCode());
  }
}
