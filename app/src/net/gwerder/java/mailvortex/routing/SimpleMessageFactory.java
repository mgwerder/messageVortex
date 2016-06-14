package net.gwerder.java.mailvortex.routing;

import net.gwerder.java.mailvortex.asn1.IdentityStore;
import net.gwerder.java.mailvortex.asn1.IdentityStoreBlock;

/**
 * Created by martin.gwerder on 06.06.2016.
 */
public class SimpleMessageFactory extends MessageFactory {

    /* Graph set to be honored */
    GraphSet graph = new GraphSet();

    /* number of ms for the graph to be completed */
    long     maxMessageTransferTime = 600*1000;

    protected SimpleMessageFactory(String msg, int source, int target, IdentityStoreBlock[] anonGroupMembers, IdentityStore is) {
        this.msg = msg;

        graph.setIdentityStore(is);
        graph.setAnonymitySet( anonGroupMembers );
        graph.setSource(anonGroupMembers[source]);
        graph.setTarget(anonGroupMembers[target]);
    }

    public void build() {

        // building vector graphs
        int numberOfGraphs = (int) (graph.getAnonymitySetSize() * 2.5);

        while (graph.size() < numberOfGraphs && !graph.allTargetsReached()) {
            IdentityStoreBlock from = null;
            IdentityStoreBlock to = null;
            while (from == null || !graph.targetReached( from )) {
                from = graph.getAnonIdentity( sr.nextInt( graph.getAnonymitySetSize() ) );
            }
            while (to == null || to == from) {
                to = graph.getAnonIdentity( sr.nextInt( graph.getAnonymitySetSize() ) );
            }
            graph.add( new Graph( from, to ) );
        }

        // FIXME honour hotspot
        // FIXME set times
        // FIXME determine message route
        // FIXME select operations

    }

    public GraphSet getGraph() {return graph;}

}
