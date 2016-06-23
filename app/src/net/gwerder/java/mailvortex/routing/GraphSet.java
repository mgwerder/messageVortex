package net.gwerder.java.mailvortex.routing;

import net.gwerder.java.mailvortex.asn1.IdentityStore;
import net.gwerder.java.mailvortex.asn1.IdentityStoreBlock;
import java.util.*;

/**
 * Represents the graphs between the nodes.
 *
 * Created by martin.gwerder on 13.06.2016.
 */
public class GraphSet extends Vector<Graph> implements Comparator<GraphSet>,Comparable<GraphSet> {

    private IdentityStore            identityStore=null;
    private List<IdentityStoreBlock> anonymitySet;
    private IdentityStoreBlock       source=null;
    private IdentityStoreBlock       target=null;

    public GraphSet() {
        anonymitySet = new Vector<>();
    }

    public void setIdentityStore(IdentityStore is) {
        this.identityStore=is;
    }

    public void setAnonymitySet(IdentityStoreBlock[] anonymitySet ) {
        Vector<IdentityStoreBlock> tmp =new Vector<IdentityStoreBlock>();
        tmp.addAll( Arrays.asList(anonymitySet) );
        this.anonymitySet=tmp;
    }

    public IdentityStoreBlock[] getAnonymitySet() { return anonymitySet.toArray( new IdentityStoreBlock[anonymitySet.size()] ); }

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

    public IdentityStoreBlock getAnonIdentity(int i) throws ArrayIndexOutOfBoundsException {
        if(i<0 || i>=anonymitySet.size()) throw new ArrayIndexOutOfBoundsException( "got invalid identity vector ("+i+")" );
        return anonymitySet.get(i);
    }

    public void setSource(IdentityStoreBlock source) throws NullPointerException, IllegalArgumentException {
        if(source==null) throw new NullPointerException( "source may not be null" );
        if(!anonymitySet.contains(source)) throw new IllegalArgumentException( "source must be member of anonymity set" );
        this.source=source;
    }

    public void setTarget(IdentityStoreBlock target) throws NullPointerException, IllegalArgumentException {
        if(target==null) throw new NullPointerException( "target may not be null" );
        if(!anonymitySet.contains(target)) throw new IllegalArgumentException( "target must be member of anonymity set" );
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
        for (Graph g : this) {
            if (g.getTo() == is) return true;
        }
        return false;
    }

    public GraphSet[] getRoutes() {
        Set<GraphSet> ret=new TreeSet<>();
        for(int i=0;i<size();i++) {
            if(get(i).getFrom().equals(source)) {
                Graph[][] g=getRoute(i,new Graph[] {get(i)},target);
                for(int j=0;j<g.length;j++) {
                    GraphSet gs=new GraphSet();
                    gs.setAnonymitySet( getAnonymitySet() );
                    gs.setSource(getSource());
                    gs.setTarget(getTarget());
                    gs.add(get(i));
                    gs.addAll(Arrays.asList(g[j]));
                    ret.add(gs);
                }
            }
        }
        return ret.toArray( new GraphSet[ret.size()] );
    }

    private Graph[][] getRoute(int startIndex,Graph[] visited,IdentityStoreBlock to) {
        List<Graph[]> ret=new Vector<>(  );
        Graph g=get(startIndex);
        if(g.getTo().equals(to)) return new Graph[][] {new Graph[0]};
        if(startIndex<size()-2){
            for(int i=startIndex+1;i<size();i++) {
                Graph tmp=get(i);
                if(tmp==null) throw new NullPointerException("access to bad index");
                boolean vis=false;
                for(Graph v:visited) {
                    if(v==null) throw new NullPointerException("OUCH got an null visited graph ... thats impossible (size is "+visited.length+";v[0]="+visited[0]+";v[1]="+visited[1]+")");
                    if(tmp.getTo().equals(v.getFrom()) || tmp.getTo().equals(v.getTo())) vis=true;
                }
                if(!vis && g.getTo().equals(tmp.getFrom())) {

                    // this node is not yet visited (check possibility)
                    List<Graph> tg1=new Vector<>(  );
                    tg1.addAll( Arrays.asList( visited ) );
                    tg1.add( tmp );
                    Graph[][] tg=getRoute(i,tg1.toArray(new Graph[tg1.size()]),to);

                    //prepend to each solution mine
                    int j=0;
                    while(j<tg.length) {
                        Graph[] gj=tg[j];
                        Graph[] gk=new Graph[gj.length+1];
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
        }
        return ret.toArray(new Graph[ret.size()][]);
    }

    public int compare(GraphSet g1,GraphSet g2) {
        if(g1.equals( g2 )) return 0;
        return (""+g1.hashCode()).compareTo( ""+g2.hashCode() );
    }

    public int compareTo(GraphSet gs) { return compare(this,gs); }

    public boolean equals(GraphSet g) {
        if( g.size()!=size()) return false;
        for(int i=0;i<size();i++) {
            if(g.get(i)==null ||get(i)==null || !get(i).equals(g.get(i))) return false;
        }
        return true;
    }

    public boolean equals(Object g) {
        if(g==null) return false;
        if(g instanceof GraphSet) {
            return equals((GraphSet)g);
        } else {
            return g==this;
        }
    }

}
