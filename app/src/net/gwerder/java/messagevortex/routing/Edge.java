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

import net.gwerder.java.messagevortex.asn1.IdentityStoreBlock;

/**
 * Represents a graph between two identity blocks inclusive senders operation and time.
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
    public Edge(IdentityStoreBlock from, IdentityStoreBlock to, long startTime, long maxDelay) {

        if(from==null) {
            throw new NullPointerException( "from may not be null in an edge" );
        }

        if(to==null) {
            throw new NullPointerException( "from may not be null in an edge" );
        }

        if( from == to || from.equals(to) ) {
            throw new IllegalArgumentException( "an edge may not have the same start and ending point" );
        }

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
        if(t==null) {
            return false;
        }
        if( t.getClass() != this.getClass() ) {
            return false;
        }
        Edge g=(Edge)t;
        return g.to.equals(this.to) && g.from.equals(this.from) && (g.startTime==this.startTime) && (g.maxDelay==this.maxDelay);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return from + " -"+startTime+"/"+maxDelay+"-> "+ to;
    }
}
