package net.messagevortex.router;

import net.messagevortex.ExtendedSecureRandom;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.RoutingCombo;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class SimpleMessageFactory extends MessageFactory {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }

  /* Edge set to be honored */
  private final GraphSet graph = new GraphSet();

  /* number of s for the graph to be completed */
  private long minMessageTransferStart = 180L;

  /* number of s for the graph to be completed */
  private long maxMessageTransferTime = 1800L;

  /* number of s between arrival the first sending time */
  private long minStepProcessSTime = 30L;

  /***
   * <p>Build a message with the specified parameters.</p>
   *
   * @param msg the message to be embedded
   * @param source the indes of the source identity
   * @param target the index of the target identity
   * @param anonGroupMembers a set of all available targets in the group set
   * @param is the identity store to be used
   * @return the built message wrapped in a message factory
   */
  public static MessageFactory buildMessage(String msg, int source, int target,
                                            IdentityStoreBlock[] anonGroupMembers,
                                            IdentityStore is) {

    MessageFactory fullmsg = new SimpleMessageFactory(msg, source, target, anonGroupMembers, is);

    // selecting hotspot
    fullmsg.hotspot = anonGroupMembers[ExtendedSecureRandom.nextInt(anonGroupMembers.length)];

    fullmsg.build();

    return fullmsg;
  }


  /***
   * <p>A simple message factory creating a possibly redundant message path.</p>
   * @param msg     the message to be used
   * @param source  the source address for the path
   * @param target  the target address for the path
   * @param anonGroupMembers the anonymity set to be used
   * @param is      the identity store providing the necessary keys and identities
   */
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
    LOGGER.log(Level.FINE, "Create graph");
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

    // set times (all times in seconds)
    LOGGER.log(Level.FINE, "Assigning time to graph");
    long minArrival = 0;
    long maxArrival = 0;
    for (int i = 0; i < graph.size(); i++) {
      // the maximum remaining time to fulfill maximum time graph length
      long maxRemainingTime = maxMessageTransferTime - maxArrival; // OK
      int remainingHops = graph.size() - i; //OK
      LOGGER.log(Level.FINEST, "calculating timing for minArival=" + minArrival
          + "/maxArrival=" + maxArrival + "/remainingTime=" + maxRemainingTime
          + "/remainingHps=" + remainingHops);
      long maxShare = maxRemainingTime - remainingHops * minStepProcessSTime - 2;
      long share = Math.max(1, maxShare / remainingHops);
      maxShare = Math.max(2, maxShare);
      LOGGER.log(Level.FINEST, "calculated shares are maxShare=" + maxShare + "/share=" + share);
      assert share > 0 : "share is negative (" + share + ")";
      long minTime = (long) (ExtendedSecureRandom.nextRandomTime(
          minArrival + minMessageTransferStart, minArrival + minMessageTransferStart + share,
          minArrival + minMessageTransferStart + maxShare));
      maxRemainingTime = maxMessageTransferTime - minTime; // OK
      maxShare = maxRemainingTime - remainingHops * minStepProcessSTime - 2;
      share = Math.max(1, maxShare / remainingHops);
      maxShare = Math.max(2, maxShare);
      long maxTime = (long) (ExtendedSecureRandom.nextRandomTime(minTime, minTime + share + 1,
          minTime + maxShare + 2));
      Edge g = graph.get(i);
      g.setStartTime(minArrival - minTime);
      g.setDelayTime(maxTime - minTime);
      maxArrival = maxTime + minStepProcessSTime;
      minArrival = minTime + minStepProcessSTime;
    }

    // select operations
    // FIXME
    return buildRoutingBlock();
  }

  private RoutingCombo buildRoutingBlock() {
    // FIXME incomplete
    // determine message route
    //GraphSet[] gs = graph.getRoutes();
    //GraphSet msgpath = gs[ExtendedSecureRandom.nextInt(gs.length)];

    return graph.getRoutingBlock();
  }

  /***
   * <p>Sets the maximum time allowed to transfer the message to the final destination.</p>
   *
   * @param newmax the new maximum transfer time in seconds
   * @return the previously set transfer time
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
   * @return the previously set time
   */
  public long setMinStepProcessSTime(long newmin) {
    long ret = minStepProcessSTime;
    minStepProcessSTime = newmin;
    return ret;
  }

  /***
   * <p>Sets the minimum time required to process a message in a node.</p>
   *
   * <p>This time includes anti-malware related processing or anti-UBE related actions.</p>
   * @param newmin the new time in seconds to be set
   * @return the previously set time
   */
  public long getMinMessageTransferStart(long newmin) {
    long ret = minMessageTransferStart;
    minMessageTransferStart = newmin;
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
    //MessageVortexLogger.setGlobalLogLevel(Level.FINEST);
    LOGGER.log(Level.INFO, "Loading identity store");
    IdentityStore identityStore = new IdentityStore(new File("identityStore.cfg"));
    LOGGER.log(Level.INFO, "getting anon set");
    IdentityStoreBlock[] anonSet = identityStore.getAnonSet(5).toArray(new IdentityStoreBlock[0]);
    LOGGER.log(Level.INFO, "creating message factory");
    SimpleMessageFactory smf = new SimpleMessageFactory("", 0, 1, anonSet, identityStore);
    LOGGER.log(Level.INFO, "building routing block");
    smf.setMaxTransferTime(1800);
    smf.setMinStepProcessSTime(30);
    smf.build();
    LOGGER.log(Level.INFO, "done building");
  }

}
