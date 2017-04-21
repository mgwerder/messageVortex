package net.gwerder.java.messagevortex.routing.operation;

/**
 * Created by martin.gwerder on 19.04.2017.
 */
public abstract class AbstractOperation {

    protected int[] id=new int[0];

    public AbstractOperation(int[] id) {
        this.id=id;
    }

    public AbstractOperation(int startid,int number) {
        this.id=new int[number];
        for(int j=startid;j<startid+number;j++) this.id[j-startid]=startid;
    }

}
