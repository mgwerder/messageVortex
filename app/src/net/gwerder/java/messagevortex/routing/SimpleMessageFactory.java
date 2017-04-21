package net.gwerder.java.messagevortex.routing;

import net.gwerder.java.messagevortex.asn1.IdentityStore;
import net.gwerder.java.messagevortex.asn1.IdentityStoreBlock;

/**
 * Created by martin.gwerder on 06.06.2016.
 */
public class SimpleMessageFactory extends MessageFactory {

    /* Edge set to be honored */
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

        while (graph.size() < numberOfGraphs || !graph.allTargetsReached()) {
            IdentityStoreBlock from = null;
            IdentityStoreBlock to = null;
            while (from == null || !graph.targetReached( from )) {
                from = graph.getAnonIdentity( esr.nextInt( graph.getAnonymitySetSize() ) );
            }
            while (to == null || to == from || to.equals( from )) {
                to = graph.getAnonIdentity( esr.nextInt( graph.getAnonymitySetSize() ) );
            }
            graph.add( new Edge( from, to , graph.size(),0 ) );
        }

        // set times
        // THIS SECTION IS BROKEN!!!!!!
        long fullTime=maxMessageTransferTime*esr.nextInt(1000)/1000;
        for(int i=0;i<graph.size();i++) {
            Edge g=graph.get(i);
            double nea=esr.nextGauss();
            double mirr=0.1;
            double range=0.5;
            double q1=0.5-mirr;
            double q3=0.5+mirr;
            long start=0;
            long delay=0;
            for(int j=0;j<2;j++) {
                double lin=(0.0+fullTime)/(graph.size()-i);
                long tmp=0;
                if(nea<q1) {
                    tmp=(long)(nea/q1*range*lin);
                } else {
                    tmp=(long)(lin+(fullTime-lin)*0.5+(nea-q3)/(1-q3)*range*(fullTime-lin));
                }
                if(j==0) {
                    start = tmp;
                } else {
                    delay = tmp;
                }
                fullTime -= tmp;
            }
            System.out.println( "setting times to "+start+"/"+delay);
            g.setStartTime( start );
            g.setDelayTime( delay );
        }

        // determine message route
        // FIXME select operation

    }

    public GraphSet getGraph() {return graph;}

}
