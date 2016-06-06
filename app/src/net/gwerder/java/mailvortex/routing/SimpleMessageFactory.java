package net.gwerder.java.mailvortex.routing;

import net.gwerder.java.mailvortex.asn1.IdentityStore;
import net.gwerder.java.mailvortex.asn1.IdentityStoreBlock;

import java.util.List;
import java.util.Vector;

/**
 * Created by martin.gwerder on 06.06.2016.
 */
public class SimpleMessageFactory extends MessageFactory {

    List<Graph> graphs = new Vector<>();

    protected SimpleMessageFactory(String msg, int source, int target, IdentityStoreBlock[] anonGroupMembers, IdentityStore is) {
        this.msg = msg;
        this.source = anonGroupMembers[source];
        this.target = anonGroupMembers[target];
        this.anonGroupMembers = anonGroupMembers;
        this.identityStore = is;
    }

    public void build() {

        // building vector graphs
        int numberOfGraphs = (int) (anonGroupMembers.length * 2.5);

        while (graphs.size() < numberOfGraphs && !allTargetsReached()) {
            IdentityStoreBlock from = null;
            IdentityStoreBlock to = null;
            while (from == null && !targetReached( from ))
                from = anonGroupMembers[sr.nextInt( anonGroupMembers.length )];
            while (to == null && to != from) to = anonGroupMembers[sr.nextInt( anonGroupMembers.length )];
            graphs.add( new Graph( from, to ) );
        }

        throw new NullPointerException( "build not yet available" );
    }

    private boolean allTargetsReached() {
        for (IdentityStoreBlock is : anonGroupMembers) {
            if (!targetReached( is )) return false;
        }
        return true;
    }

    private boolean targetReached(IdentityStoreBlock is) {
        if (is == source) return true;
        for (Graph g : graphs) {
            if (g.to == is) return true;
        }
        return false;
    }

    private class Graph {

        IdentityStoreBlock from;
        IdentityStoreBlock to;

        public Graph(IdentityStoreBlock from, IdentityStoreBlock to) {
            this.from = from;
            this.to = to;
        }

    }

}
