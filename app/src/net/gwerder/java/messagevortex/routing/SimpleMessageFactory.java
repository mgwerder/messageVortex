package net.gwerder.java.messagevortex.routing;
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

import net.gwerder.java.messagevortex.ExtendedSecureRandom;
import net.gwerder.java.messagevortex.asn1.IdentityStore;
import net.gwerder.java.messagevortex.asn1.IdentityStoreBlock;

/**
 * Created by martin.gwerder on 06.06.2016.
 */
public class SimpleMessageFactory extends MessageFactory {

  /* Edge set to be honored */
  GraphSet graph = new GraphSet();

  /* number of ms for the graph to be completed */
  long maxMessageTransferTime = 600L * 1000L;

  protected SimpleMessageFactory(String msg, int source, int target, IdentityStoreBlock[] anonGroupMembers, IdentityStore is) {
    this.msg = msg;

    graph.setAnonymitySet(anonGroupMembers);
    graph.setSource(anonGroupMembers[source]);
    graph.setTarget(anonGroupMembers[target]);
  }

  public void build() {

    // building vector graphs
    int numberOfGraphs = (int) (graph.getAnonymitySetSize() * 2.5);

    while (graph.size() < numberOfGraphs || !graph.allTargetsReached()) {
      IdentityStoreBlock from = null;
      IdentityStoreBlock to = null;
      while (from == null || !graph.targetReached(from)) {
        from = graph.getAnonIdentity(ExtendedSecureRandom.nextInt(graph.getAnonymitySetSize()));
      }
      while (to == null || to == from || to.equals(from)) {
        to = graph.getAnonIdentity(ExtendedSecureRandom.nextInt(graph.getAnonymitySetSize()));
      }
      graph.add(new Edge(from, to, graph.size(), 0));
    }

    // set times
    // FIXME: THIS SECTION IS BROKEN!!!!!!
    long fullTime = maxMessageTransferTime * ExtendedSecureRandom.nextInt(1000) / 1000;
    for (int i = 0; i < graph.size(); i++) {
      Edge g = graph.get(i);
      long start = (long) ExtendedSecureRandom.nextRandomTime(30000, 60000, 90000);
      long avg = fullTime / (graph.size() - i);
      long delay = (long) ExtendedSecureRandom.nextRandomTime(30000, 30000 + avg, 30000 + 2 * avg);
      System.out.println("setting times to " + start + "/" + delay);
      g.setStartTime(start);
      g.setDelayTime(delay);
      fullTime += start + delay;
    }

    // determine message route
    // FIXME select operation

  }

  public GraphSet getGraph() {
    return graph;
  }

}
