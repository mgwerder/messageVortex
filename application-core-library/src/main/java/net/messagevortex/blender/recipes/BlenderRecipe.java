package net.messagevortex.blender.recipes;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.RoutingCombo;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class BlenderRecipe implements Comparable<BlenderRecipe> {

  static final Map<String, Set<BlenderRecipe>> recipes = new HashMap<>();

  private static final SecureRandom esr = ExtendedSecureRandom.getSecureRandom();

  private static final String DEFAULT = "default";

  /***
   * <p>Get a recipe from the specified recipe set.</p>
   *
   * @param identifier the name of the recipe set
   * @param anonSet    the anonymity set to be used
   * @return a random recipe
   *
   * @throws IOException if no candidates can be found
   */
  public static BlenderRecipe getRecipe(String identifier, Set<IdentityStoreBlock> anonSet)
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

  /***
   * <p>Remove all recipes from the specified list of recipes.</p>
   *
   * @param identifier the recipe list identifier (null for default list)
   */
  public static void clearRecipes(String identifier) {
    if (identifier == null) {
      identifier = DEFAULT;
    }

    synchronized (recipes) {
      Set<BlenderRecipe> r=recipes.remove(identifier);
      if (r!=null) {
        r.clear();
      }
    }
  }

  /***
   * <p>Adds a recipe to the specified recipe list.</p>
   *
   * @param identifier the name of the recipe list (null for default)
   * @param add the recipe to be added
   */
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

  /***
   * <p>Tests if the given recipe may be applied to the anon set available.</p>
   *
   * @param anonSet the currently available anonymity set
   * @return true if the recipe may be applied
   */
  public abstract boolean isAppliable(Set<IdentityStoreBlock> anonSet);

  /***
   * <p>Creates a routing block with the given parameters.</p>
   *
   * @param anonSet the anonymity set to be used
   * @param from the sending node address
   * @param to the receiving node address
   * @return the built routing block
   *
   * @throws IOException if a problem arises when creating the block
   */
  public abstract RoutingCombo applyRecipe(Set<IdentityStoreBlock> anonSet,
                                           IdentityStoreBlock from,
                                           IdentityStoreBlock to) throws IOException;

  @Override
  public int compareTo(BlenderRecipe o) {
    return ("" + hashCode()).compareTo("" + o.hashCode());
  }
}
