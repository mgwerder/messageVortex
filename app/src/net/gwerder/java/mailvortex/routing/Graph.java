package net.gwerder.java.mailvortex.routing;

import net.gwerder.java.mailvortex.asn1.IdentityStoreBlock;

/**
 * Represents a graph betwee two identity blocks inclusive senders operations and time.
 *
 * Created by martin.gwerder on 13.06.2016.
 */
class Graph {

    private IdentityStoreBlock from;
    private IdentityStoreBlock to;

    /***
     * Represents a routing graph during sending.
     *
     * @param  from  The starting point of the graph
     * @param  to    The ending point of the graph
     * @throws IllegalArgumentException if from and two are equal
     * @throws NullPointerException if one of the parameters is null
     */
    public Graph(IdentityStoreBlock from, IdentityStoreBlock to) throws IllegalArgumentException,NullPointerException {
        if(from==to) throw new IllegalArgumentException( "a graph may not have the same start and ending point" );
        if(from==null) throw new NullPointerException( "from may not be null" );
        if(to==null) throw new NullPointerException( "from may not be null" );
        this.from = from;
        this.to = to;
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

    public boolean equals(Graph g) {
        return g.to.equals(to) && g.from.equals( from );
    }

}
