package net.gwerder.java.messagevortex.routing;

import net.gwerder.java.messagevortex.asn1.IdentityStore;
import net.gwerder.java.messagevortex.asn1.IdentityStoreBlock;

import java.util.*;

/**
 * Represents the graphs between the nodes.
 *
 * Created by martin.gwerder on 13.06.2016.
 */
public class GraphSet implements Comparator<GraphSet>,Comparable<GraphSet>,Iterable<Edge> {

    private Vector<Edge> store=new Vector<>();
    private static final long serialVersionUID = 16134223345689L;

    private IdentityStore            identityStore=null;
    private List<IdentityStoreBlock> anonymitySet;
    private IdentityStoreBlock       source=null;
    private IdentityStoreBlock       target=null;
    private boolean                  hasChanged=true;
    private Object                   cacheLock=new Object();
    private GraphSet[]               cache=null;

    public GraphSet() {
        anonymitySet = new Vector<>();
    }

    public void setIdentityStore(IdentityStore is) {
        this.identityStore=is;
    }

    public void setAnonymitySet(IdentityStoreBlock[] anonymitySet ) {
        Vector<IdentityStoreBlock> tmp =new Vector<>();
        tmp.addAll( Arrays.asList(anonymitySet) );
        this.anonymitySet=tmp;
    }

    public IdentityStoreBlock[] getAnonymitySet() {
        return anonymitySet.toArray( new IdentityStoreBlock[anonymitySet.size()] );
    }

    public IdentityStoreBlock getAnonymity(int i) {
        return anonymitySet.get(i);
    }

    public IdentityStoreBlock getSource() {
        return source;
    }

    public IdentityStoreBlock getTarget() {
        return target;
    }

    public int  getAnonymitySetSize() {
        return anonymitySet.size();
    }

    public int  getAnonymityIndex(IdentityStoreBlock isb) {
        return anonymitySet.indexOf( isb );
    }

    public boolean add(Edge g) {
        hasChanged=true;
        return store.add(g);
    }

    public boolean contains(Edge g) {
        for(Edge e:store) {
            if(e.equals(g)) return true;
        }
        return false;
    }

    public boolean addAll(Collection<? extends Edge> g) {
        hasChanged=true;
        return store.addAll(g);
    }

    public IdentityStoreBlock getAnonIdentity(int i) throws ArrayIndexOutOfBoundsException {
        if(i<0 || i>=anonymitySet.size()) throw new ArrayIndexOutOfBoundsException( "got invalid identity vector ("+i+")" );
        return anonymitySet.get(i);
    }

    public void setSource(IdentityStoreBlock source) throws NullPointerException, IllegalArgumentException {
        if(source==null) throw new NullPointerException( "source may not be null" );
        if(!anonymitySet.contains(source)) throw new IllegalArgumentException( "source must be member of anonymity set" );
        this.hasChanged=true;
        this.source=source;
    }

    public void setTarget(IdentityStoreBlock target) throws NullPointerException, IllegalArgumentException {
        if(target==null) throw new NullPointerException( "target may not be null" );
        if(!anonymitySet.contains(target)) throw new IllegalArgumentException( "target must be member of anonymity set" );
        this.hasChanged=true;
        this.target=target;
    }

    public boolean allTargetsReached() {
        for (IdentityStoreBlock is : anonymitySet) {
            if (!targetReached( is )) return false;
        }
        return true;
    }

    public boolean targetReached( IdentityStoreBlock is) throws NullPointerException {
        if(is==null) throw new NullPointerException();
        if (is.equals(source)) return true;
        for (Edge g : store) {
            if (g.getTo() == is) return true;
        }
        return false;
    }

    public GraphSet[] getRoutes() {
        synchronized(cacheLock) {
            if (hasChanged) {
                Set<GraphSet> ret = new TreeSet<>();
                for (int i = 0; i < store.size(); i++) {
                    if (store.get( i ).getFrom().equals( getSource() )) {
                        Edge[][] g = getRoute( i, new Edge[]{store.get( i )}, getTarget() );
                        for (Edge[] gr : g) {
                            GraphSet gs = new GraphSet();
                            gs.setAnonymitySet( getAnonymitySet() );
                            gs.setSource( getSource() );
                            gs.setTarget( getTarget() );
                            gs.add( store.get( i ) );
                            gs.addAll( Arrays.asList( gr ) );
                            ret.add( gs );
                        }
                    }
                }
                hasChanged = false;
                cache = ret.toArray( new GraphSet[ret.size()] );
            }
            return cache;
        }
    }

    private Edge[][] getRoute(int startIndex, Edge[] visited, IdentityStoreBlock to) {
        List<Edge[]> ret=new Vector<>(  );

        // get last graph
        Edge g=store.get(startIndex);

        // if target reached tell so
        if(g.getTo().equals(to)) return new Edge[][] {new Edge[0]};

        //
        for(int i=startIndex+1;i<store.size();i++) {
            Edge tmp=store.get(i);
            if(tmp==null) throw new NullPointerException("access to bad index");

            // avoid loops in current path (no visited graphs)
            boolean vis=false;
            for(Edge v:visited) {
                if(v==null) throw new NullPointerException("OUCH got an null visited graph ... thats impossible (size is "+visited.length+";v[0]="+visited[0]+";v[1]="+visited[1]+")");
                if(tmp.getTo().equals(v.getFrom()) || tmp.getTo().equals(v.getTo())) vis=true;
            }

            // if not yet visited and going off from current node -> evaluate possibilities
            if(!vis && g.getTo().equals(tmp.getFrom())) {

                // this node is not yet visited (check possibility)

                // building new visited array
                List<Edge> tg1=new Vector<>(  );
                tg1.addAll( Arrays.asList( visited ) );
                tg1.add( tmp );

                // recursive call from new position
                Edge[][] tg=getRoute(i,tg1.toArray(new Edge[tg1.size()]),to);

                //prepend to each solution mine
                int j=0;
                while(j<tg.length) {
                    Edge[] gj=tg[j];
                    Edge[] gk=new Edge[gj.length+1];
                    if(gj.length>0) {
                        System.arraycopy(gj, 0, gk, 1, gj.length);
                    }
                    gk[0]=tmp;
                    tg[j]=gk;
                    j++;
                }
                ret.addAll( Arrays.asList(tg) );
            }
        }
        return ret.toArray(new Edge[ret.size()][]);
    }

    public int compare(GraphSet g1,GraphSet g2) {
        if(g1.equals( g2 )) return 0;
        return (""+g1.hashCode()).compareTo( ""+g2.hashCode() );
    }

    public int compareTo(GraphSet gs) { return compare(this,gs); }

    @Override
    public synchronized int hashCode() {
        return super.hashCode();
    }

    public boolean equals(Object g) {
        if(g==null) return false;
        if(g instanceof GraphSet) {
            GraphSet t=(GraphSet)g;
            if( t.store.size()!=store.size()) return false;
            for(int i=0;i<store.size();i++) {
                if(t.store.get(i)==null ||get(i)==null || !get(i).equals(t.store.get(i))) return false;
            }
            return true;
        } else {
            return g==this;
        }
    }

    public void dump() {
        for(Edge g:store) {
            System.out.println( "  "+anonymitySet.indexOf( g.getFrom() ) + " -> " + anonymitySet.indexOf( g.getTo() ) );
        }
        System.out.println("}");
    }

    public int size() {return store.size();}
    public Edge get(int i) {return store.get( i );}
    public Iterator iterator() {return store.iterator();}


}
