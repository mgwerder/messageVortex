package net.messagevortex.router;

// ************************************************************************************
// * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// ************************************************************************************

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import net.messagevortex.asn1.IdentityStoreBlock;

/**
 * <p>Represents the graphs between the nodes.</p>
 */
public class GraphSet implements Comparator<GraphSet>, Comparable<GraphSet>, Iterable<Edge> {

  private static final long serialVersionUID = 16134223345689L;

  private List<Edge> store = new ArrayList<>();
  private List<IdentityStoreBlock> anonymitySet;
  private IdentityStoreBlock source = null;
  private IdentityStoreBlock target = null;
  private boolean hasChanged = true;
  private Object cacheLock = new Object();
  private GraphSet[] cache = null;

  /***
   * <p>Creates a new empty graph set.</p>
   */
  public GraphSet() {
    anonymitySet = new Vector<>();
  }

  public IdentityStoreBlock[] getAnonymitySet() {
    return anonymitySet.toArray(new IdentityStoreBlock[anonymitySet.size()]);
  }

  public void setAnonymitySet(IdentityStoreBlock[] anonymitySet) {
    List<IdentityStoreBlock> tmp = new ArrayList<>();
    tmp.addAll(Arrays.asList(anonymitySet));
    this.anonymitySet = tmp;
  }

  public IdentityStoreBlock getAnonymity(int i) {
    return anonymitySet.get(i);
  }

  public IdentityStoreBlock getSource() {
    return source;
  }

  /***
   * <p>Sets the source identity of this graph.</p>
   *
   * @param source                    the source identity to be set
   * @throws IllegalArgumentException if the source is not part of the anonymity set
   * @throws NullPointerException     if the source is null
   */
  public void setSource(IdentityStoreBlock source) {
    if (source == null) {
      throw new NullPointerException("source may not be null");
    }
    if (!anonymitySet.contains(source)) {
      throw new IllegalArgumentException("source must be member of anonymity set");
    }
    this.hasChanged = true;
    this.source = source;
  }

  public IdentityStoreBlock getTarget() {
    return target;
  }

  /***
   * <p>Sets the target identity of this graph.</p>
   *
   * @param target the target identity to be set
   * @throws IllegalArgumentException if the target is not part of the anonymity set
   * @throws NullPointerException     if the target is null
   */
  public void setTarget(IdentityStoreBlock target) {
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

  public int getAnonymityIndex(IdentityStoreBlock isb) {
    return anonymitySet.indexOf(isb);
  }

  public boolean add(Edge g) {
    hasChanged = true;
    return store.add(g);
  }

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
  public IdentityStoreBlock getAnonIdentity(int i) {
    if (i < 0 || i >= anonymitySet.size()) {
      throw new ArrayIndexOutOfBoundsException("got invalid identity vector (" + i + ")");
    }
    return anonymitySet.get(i);
  }

  public boolean allTargetsReached() {
    for (IdentityStoreBlock is : anonymitySet) {
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
  public boolean targetReached(IdentityStoreBlock is) {
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

  public GraphSet[] getRoutes() {
    synchronized (cacheLock) {
      if (hasChanged) {
        Set<GraphSet> ret = new TreeSet<>();
        for (int i = 0; i < store.size(); i++) {
          if (store.get(i).getFrom().equals(getSource())) {
            Edge[][] g = getRoute(i, new Edge[]{store.get(i)}, getTarget());
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
      return new Edge[][]{new Edge[0]};
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
        List<Edge> tg1 = new ArrayList<>();
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

  public int compare(GraphSet g1, GraphSet g2) {
    if (g1.equals(g2)) {
      return 0;
    }
    return ("" + g1.hashCode()).compareTo("" + g2.hashCode());
  }

  public int compareTo(GraphSet gs) {
    return compare(this, gs);
  }

  @Override
  public synchronized int hashCode() {
    return super.hashCode();
  }

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

  public String dump() {
    StringBuilder sb = new StringBuilder();
    for (Edge g : store) {
      sb.append("  " + anonymitySet.indexOf(g.getFrom()) + " -> "
              + anonymitySet.indexOf(g.getTo()));
    }
    sb.append("}");
    return sb.toString();
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
