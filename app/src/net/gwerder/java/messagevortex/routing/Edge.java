package net.gwerder.java.messagevortex.routing;

import net.gwerder.java.messagevortex.asn1.IdentityStoreBlock;

/**
 * Represents a graph betwee two identity blocks inclusive senders operations and time.
 *
 * Created by martin.gwerder on 13.06.2016.
 */
public class Edge {

    private static final long serialVersionUID = 9070431563L;

    private IdentityStoreBlock from;
    private IdentityStoreBlock to;
    private long               startTime = 0;
    private long               maxDelay  = 10000;

    /***
     * Represents a routing graph during sending.
     *
     * @param  from       The starting point of the graph
     * @param  to         The ending point of the graph
     * @param  startTime  The starting time relative to the GraphSet start (in ms)
     * @param  maxDelay   The maximum delay after the start of this graph (in ms)
     * @throws IllegalArgumentException if from and two are equal
     * @throws NullPointerException if one of the parameters is null
     */
    public Edge(IdentityStoreBlock from, IdentityStoreBlock to, long startTime, long maxDelay) throws IllegalArgumentException,NullPointerException {
        if(from==to) throw new IllegalArgumentException( "an edge may not have the same start and ending point" );
        if(from==null) throw new NullPointerException( "from may not be null in an edge" );
        if(to==null) throw new NullPointerException( "from may not be null in an edge" );
        this.from = from;
        this.to = to;
        this.startTime= startTime;
        this.maxDelay= maxDelay;
    }

    /***
     * Get the sending entity.
     *
     * @return The sending entity
     */
    public IdentityStoreBlock getFrom() { return from; }

    /***
     * Get the receiving entity.
     *
     * @return The receiving entity
     */
    public IdentityStoreBlock getTo() {
        return to;
    }

    public long getStartTime() { return startTime; }

    public long setStartTime( long newStartTime ) {
        long old=startTime;
        this.startTime=newStartTime;
        return old;
    }

    public long getDelayTime() { return maxDelay; }

    public long setDelayTime( long newMaxDelay ) {
        long old=maxDelay;
        this.maxDelay=newMaxDelay;
        return old;
    }

    public boolean equals(Object t) {
        if(t==null) return false;
        if(! (t instanceof Edge)) return false;
        Edge g=(Edge)t;
        //System.out.println("comparing "+this+":"+toString());
        //System.out.println("     with "+g   +":"+g.toString());
        return g.to.equals(this.to) && g.from.equals(this.from) && (g.startTime==this.startTime) && (g.maxDelay==this.maxDelay);
    }

    @Override
    public int hashCode() {
        return (""+from+"/"+to+"/"+startTime+"/"+maxDelay).hashCode();
    }

    @Override
    public String toString() {
        return from + " -"+startTime+"/"+maxDelay+"-> "+ to;
    }
}
