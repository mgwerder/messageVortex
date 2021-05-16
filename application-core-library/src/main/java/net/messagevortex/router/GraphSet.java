package net.messagevortex.router;

import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.RoutingCombo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

/**
 * <p>Represents the graphs between the nodes.</p>
 */
public class GraphSet implements Comparator<GraphSet>, Comparable<GraphSet>, Iterable<Edge> {

  private static final long serialVersionUID = 16134223345689L;

  private final List<Edge> store = new ArrayList<>();
  private List<net.messagevortex.asn1.IdentityStoreBlock> anonymitySet;
  private net.messagevortex.asn1.IdentityStoreBlock source = null;
  private net.messagevortex.asn1.IdentityStoreBlock target = null;
  private boolean hasChanged = true;
  private final Object cacheLock = new Object();
  private GraphSet[] cache = null;

  /***
   * <p>Creates a new empty graph set.</p>
   */
  public GraphSet() {
    anonymitySet = new Vector<>();
  }

  public net.messagevortex.asn1.IdentityStoreBlock[] getAnonymitySet() {
    return anonymitySet.toArray(new net.messagevortex.asn1.IdentityStoreBlock[anonymitySet.size()]);
  }

  /***
   * <p>Sets the list of identities to be used for the anonymity set.</p>
   *
   * @param anonymitySet a list of identities to be used
   */
  public void setAnonymitySet(net.messagevortex.asn1.IdentityStoreBlock[] anonymitySet) {
    List<net.messagevortex.asn1.IdentityStoreBlock> tmp = new ArrayList<>();
    tmp.addAll(Arrays.asList(anonymitySet));
    this.anonymitySet = tmp;
  }

  public net.messagevortex.asn1.IdentityStoreBlock getAnonymity(int i) {
    return anonymitySet.get(i);
  }

  public net.messagevortex.asn1.IdentityStoreBlock getSource() {
    return source;
  }

  /***
   * <p>Sets the source identity of this graph.</p>
   *
   * @param source                    the source identity to be set
   * @throws IllegalArgumentException if the source is not part of the anonymity set
   * @throws NullPointerException     if the source is null
   */
  public void setSource(net.messagevortex.asn1.IdentityStoreBlock source) {
    if (source == null) {
      throw new NullPointerException("source may not be null");
    }
    if (!anonymitySet.contains(source)) {
      throw new IllegalArgumentException("source must be member of anonymity set");
    }
    this.hasChanged = true;
    this.source = source;
  }

  public net.messagevortex.asn1.IdentityStoreBlock getTarget() {
    return target;
  }

  /***
   * <p>Sets the target identity of this graph.</p>
   *
   * @param target the target identity to be set
   * @throws IllegalArgumentException if the target is not part of the anonymity set
   * @throws NullPointerException     if the target is null
   */
  public void setTarget(net.messagevortex.asn1.IdentityStoreBlock target) {
    if (target == null) {
      throw new NullPointerException("target may not be null");
    }
    if (!anonymitySet.contains(target)) {
      throw new IllegalArgumentException("target must be member of anonymity set");
    }
    this.hasChanged = true;
    this.target = target;
  }

  public int getAnonymitySetSize() {
    return anonymitySet.size();
  }

  public int getAnonymityIndex(net.messagevortex.asn1.IdentityStoreBlock isb) {
    return anonymitySet.indexOf(isb);
  }

  public boolean add(Edge g) {
    hasChanged = true;
    return store.add(g);
  }

  /***
   * <p>Check for a edge covering the mentioned points.</p>
   * @param g the edge to be searched for
   * @return true if the graph has been found at least once
   */
  public boolean contains(Edge g) {
    for (Edge e : store) {
      if (e.equals(g)) {
        return true;
      }
    }
    return false;
  }

  public boolean addAll(Collection<? extends Edge> g) {
    hasChanged = true;
    return store.addAll(g);
  }

  /***
   * <p>Get an identity from the specified anonymity set.</p>
   *
   * @param i  the index of the identity to obtain
   * @return the identity store block specified
   * @throws ArrayIndexOutOfBoundsException if i is outside the bounds of the anonymity set
   */
  public net.messagevortex.asn1.IdentityStoreBlock getAnonIdentity(int i) {
    if (i < 0 || i >= anonymitySet.size()) {
      throw new ArrayIndexOutOfBoundsException("got invalid identity vector (" + i + ")");
    }
    return anonymitySet.get(i);
  }

  /***
   * <p>check if all members of the anonymity set hve been reached at least once.</p>
   *
   * @return true if all members have been reached
   */
  public boolean allTargetsReached() {
    for (net.messagevortex.asn1.IdentityStoreBlock is : anonymitySet) {
      if (!targetReached(is)) {
        return false;
      }
    }
    return true;
  }

  /***
   * <p>Checks if a specific identity store block is already reached by this graph.</p>
   *
   * @param is the identity store block
   * @return true if the identity store block has been reached already in the past
   * @throws NullPointerException if the specified identity stor block is null
   */
  public boolean targetReached(net.messagevortex.asn1.IdentityStoreBlock is) {
    if (is == null) {
      throw new NullPointerException();
    }
    if (is.equals(source)) {
      return true;
    }
    for (Edge g : store) {
      if (g.getTo() == is) {
        return true;
      }
    }
    return false;
  }

  /***
   * <p>Get a set of all graphs determined.</p>
   *
   * @return the array of graphs generated
   */
  public GraphSet[] getRoutes() {
    synchronized (cacheLock) {
      if (hasChanged) {
        Set<GraphSet> ret = new TreeSet<>();
        for (int i = 0; i < store.size(); i++) {
          if (store.get(i).getFrom().equals(getSource())) {
            Edge[][] g = getRoute(i, new Edge[] {store.get(i)}, getTarget());
            for (Edge[] gr : g) {
              GraphSet gs = new GraphSet();
              gs.setAnonymitySet(getAnonymitySet());
              gs.setSource(getSource());
              gs.setTarget(getTarget());
              gs.add(store.get(i));
              gs.addAll(Arrays.asList(gr));
              ret.add(gs);
            }
          }
        }
        hasChanged = false;
        cache = ret.toArray(new GraphSet[ret.size()]);
      }
      return cache;
    }
  }

  private Edge[][] getRoute(int startIndex, Edge[] visited, IdentityStoreBlock to) {
    List<Edge[]> ret = new ArrayList<>();

    // get last graph
    Edge g = store.get(startIndex);

    // if target reached tell so
    if (g.getTo().equals(to)) {
      return new Edge[][] {new Edge[0]};
    }

    //
    for (int i = startIndex + 1; i < store.size(); i++) {
      Edge tmp = store.get(i);
      if (tmp == null) {
        throw new NullPointerException("access to bad index");
      }

      // avoid loops in current path (no visited graphs)
      boolean vis = false;
      for (Edge v : visited) {
        if (v == null) {
          throw new NullPointerException("OUCH got an null visited graph ... "
              + "thats impossible (size is " + visited.length + ";v[0]=" + visited[0]
              + ";v[1]=" + visited[1] + ")");
        }
        if (tmp.getTo().equals(v.getFrom()) || tmp.getTo().equals(v.getTo())) {
          vis = true;
        }
      }

      // if not yet visited and going off from current node -> evaluate possibilities
      if (!vis && g.getTo().equals(tmp.getFrom())) {

        // this node is not yet visited (check possibility)

        // building new visited array
        final List<Edge> tg1 = new ArrayList<>();
        tg1.addAll(Arrays.asList(visited));
        tg1.add(tmp);

        // recursive call from new position
        Edge[][] tg = getRoute(i, tg1.toArray(new Edge[tg1.size()]), to);

        //prepend to each solution mine
        int j = 0;
        while (j < tg.length) {
          Edge[] gj = tg[j];
          Edge[] gk = new Edge[gj.length + 1];
          if (gj.length > 0) {
            System.arraycopy(gj, 0, gk, 1, gj.length);
          }
          gk[0] = tmp;
          tg[j] = gk;
          j++;
        }
        ret.addAll(Arrays.asList(tg));
      }
    }
    return ret.toArray(new Edge[ret.size()][]);
  }

  /***
   * <p> Compares two graph sets for equality.</p>
   *
   * @param g1 the first graph set required for comparison
   * @param g2 the secondgraph set required for comparison
   * @return 0 if both sets are equal
   */
  public int compare(GraphSet g1, GraphSet g2) {
    if (g1.equals(g2)) {
      return 0;
    }
    return ("" + g1.hashCode()).compareTo("" + g2.hashCode());
  }

  /***
   * <p>Compares this graph set with another graph set.</p>
   *
   * @param gs the second graph set for comparison
   * @return 0 if both sets are equal
   */
  public int compareTo(GraphSet gs) {
    return compare(this, gs);
  }

  @Override
  public synchronized int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(Object g) {
    if (g == null) {
      return false;
    }
    if (g.getClass() != this.getClass()) {
      GraphSet t = (GraphSet) g;
      if (t.store.size() != store.size()) {
        return false;
      }
      for (int i = 0; i < store.size(); i++) {
        if (t.store.get(i) == null || get(i) == null || !get(i).equals(t.store.get(i))) {
          return false;
        }
      }
      return true;
    } else {
      return g == this;
    }
  }

  /***
   * <p>Get a string representation of the graph set.</p>
   *
   * @return the requested string representation
   */
  public String dump() {
    StringBuilder sb = new StringBuilder();
    for (Edge g : store) {
      sb.append("  ").append(anonymitySet.indexOf(g.getFrom())).append(" -> ")
          .append(anonymitySet.indexOf(g.getTo()));
    }
    sb.append('}');
    return sb.toString();
  }

  public RoutingCombo getRoutingBlock() {
    // FIXME implementation missing
    return null;
  }

  public int size() {
    return store.size();
  }

  public Edge get(int i) {
    return store.get(i);
  }

  public Iterator<Edge> iterator() {
    return store.iterator();
  }
}
