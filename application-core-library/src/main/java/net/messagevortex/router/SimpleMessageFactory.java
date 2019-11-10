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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.RoutingCombo;

/**
 * Created by martin.gwerder on 06.06.2016.
 */
public class SimpleMessageFactory extends MessageFactory {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  /* Edge set to be honored */
  private GraphSet graph = new GraphSet();

  /* number of ms for the graph to be completed */
  private long minMessageTransferTime = 300L * 1000L;

  /* number of ms for the graph to be completed */
  private long maxMessageTransferTime = 600L * 1000L;

  /* number of ms between arrival the first sending time */
  private long minStepProcessSTime = 30L * 1000L;

  public SimpleMessageFactory(String msg, int source, int target,
                                 IdentityStoreBlock[] anonGroupMembers, IdentityStore is) {
    this.msg = msg;

    graph.setAnonymitySet(anonGroupMembers);
    graph.setSource(anonGroupMembers[source]);
    graph.setTarget(anonGroupMembers[target]);
  }

  /***
   * <p>build a simple message path.</p>
   */
  public RoutingCombo build() {

    // building vector graphs
    // minimum Graph size is 2.5 time edges
    int numberOfGraphs = (int) (graph.getAnonymitySetSize() * 2.5);

    // create graph until minimum size is reached and target graph get message
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
    long fullMsgTime = minMessageTransferTime + (maxMessageTransferTime - minMessageTransferTime)
            * ExtendedSecureRandom.nextInt(1000) / 1000;
    LOGGER.log(Level.FINE, "Transfer time of message is " + (fullMsgTime / 1000) + "s");
    long minArrival = 0;
    long maxArrival = 0;
    for (int i = 0; i < graph.size(); i++) {
      Edge g = graph.get(i);
      long minDelay = (long) ExtendedSecureRandom.nextRandomTime(minStepProcessSTime,
              2 * minStepProcessSTime, 3 * minStepProcessSTime);
      long avg = (fullMsgTime - maxArrival) / (graph.size() - i);
      long maxDelay = (long) ExtendedSecureRandom.nextRandomTime(0, 0 + avg - minStepProcessSTime,
              0 + 2 * avg - minStepProcessSTime);
      LOGGER.log(Level.FINER, "  setting times to arrival:" + (minArrival / 1000) + "-"
              + (maxArrival / 1000) + "; startDelay:" + (minDelay / 1000) + "-"
              + ((minDelay + maxDelay) / 1000) + " (est. delivery time is "
              + ((minArrival + (maxArrival - minArrival) / 2 + minDelay) / 1000) + "s)");
      g.setStartTime(minDelay);
      g.setDelayTime(maxDelay);

      minArrival += minDelay;
      maxArrival += minDelay + maxDelay;
    }

    // select operations
    return buildRoutingBlock();
  }

  private RoutingCombo buildRoutingBlock() {
    // determine message route
    GraphSet[] gs = graph.getRoutes();
    GraphSet msgpath = gs[ExtendedSecureRandom.nextInt(gs.length)];

    return graph.getRoutingBlock();
  }

  /***
   * <p>Sets the maximum time allowed to transfer the message to the final destination.</p>
   *
   * @param newmax the new maximum transfer time in seconds
   * @return       the previously set transfer time
   */
  public long setMaxTransferTime(long newmax) {
    long ret = maxMessageTransferTime;
    maxMessageTransferTime = newmax;
    return ret;
  }

  /***
   * <p>Sets the minimum time required to process a message in a node.</p>
   *
   * <p>This time includes anti-malware related processing or anti-UBE related actions.</p>
   * @param newmin the new time in seconds to be set
   * @return       the previously set time
   */
  public long setMinStepProcessSTime(long newmin) {
    long ret = minStepProcessSTime;
    minStepProcessSTime = newmin;
    return ret;
  }

  /***
   * <p>Gets the previously built message path.</p>
   * @return the message path or null if the previous build has failed
   */
  public GraphSet getGraph() {
    return graph;
  }

  /***
   * <p>This is a test methode sheduled to be removed.</p>
   *
   * @param args          ordinary main args (ignored)
   * @throws IOException  if the function was unable to load the identity store from filesystem
   */
  public static void main(String[] args) throws IOException {
    MessageVortexLogger.setGlobalLogLevel(Level.FINEST);
    LOGGER.log(Level.INFO, "Loading identity store");
    IdentityStore identityStore = new IdentityStore(new File("identityStore.cfg"));
    LOGGER.log(Level.INFO, "getting anon set");
    IdentityStoreBlock[] anonSet = identityStore.getAnonSet(5).toArray(new IdentityStoreBlock[0]);
    LOGGER.log(Level.INFO, "creating message factory");
    SimpleMessageFactory smf = new SimpleMessageFactory("", 0, 1, anonSet, identityStore);
    LOGGER.log(Level.INFO, "building routing block");
    smf.setMaxTransferTime(60 * 1000);
    smf.setMinStepProcessSTime(6 * 1000);
    smf.build();
    LOGGER.log(Level.INFO, "done building");
  }

}
