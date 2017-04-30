package net.gwerder.java.messagevortex.routing.operation;

import net.gwerder.java.messagevortex.asn1.Identity;
import net.gwerder.java.messagevortex.asn1.PayloadChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Martin on 29.04.2017.
 */
public class InternalPayload {

    private static Map<Identity,InternalPayload> internalPayloadMap = new ConcurrentHashMap<>();

    Identity identity;
    List<AbstractOperation> operations=new ArrayList<>();
    Map<Integer,PayloadChunk> internalPayload=new ConcurrentHashMap<>();

    private long lastcompact=System.currentTimeMillis();

    private InternalPayload( Identity i) {
        identity=i;
        internalPayloadMap.put(i,this);
    }

    public static InternalPayload getInternalPayload(Identity i) {
        synchronized(internalPayloadMap) {
            InternalPayload ret = internalPayloadMap.get(i);
            if(ret==null) {
                ret=new InternalPayload(i);
            }
            return ret;
        }
    }

    public Identity getIdentity() {
        return identity;
    }

    public PayloadChunk getPayload(int id) {
        synchronized(internalPayload) {
            return internalPayload.get(id);
        }
    }

    public PayloadChunk setPayload(int id,PayloadChunk p) {
        synchronized(internalPayload) {
            PayloadChunk old=getPayload(id);
            if(p==null) {
                internalPayload.remove(id);
            } else {
                internalPayload.put(id,p);
            }
            return old;
        }
    }

    private boolean compact() {
        if(System.currentTimeMillis()<lastcompact+60000) return false;

        // FIXME compact structure

        return true;
    }



}
