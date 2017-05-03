package net.gwerder.java.messagevortex.routing.operation;

import net.gwerder.java.messagevortex.asn1.IdentityBlock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InternalPayloadSpace {

    private static Map<IdentityBlock,InternalPayload> internalPayloadMap = new ConcurrentHashMap<>();

    public InternalPayload setPayload(IdentityBlock i, InternalPayload pl) {
        InternalPayload ret=null;
        synchronized(internalPayloadMap) {
            ret=internalPayloadMap.get(i);
            if(pl!=null) {
                internalPayloadMap.put(i, pl);
            } else {
                internalPayloadMap.remove(i);
            }
        }
        return ret;
    }

    public InternalPayload getPayload(IdentityBlock i) {
        InternalPayload ret;
        synchronized(internalPayloadMap) {
            ret=internalPayloadMap.get(i);
            if(ret==null) {
                ret=new InternalPayload(this,i);
                internalPayloadMap.put(i,ret);
            }
        }
        return ret;
    }

}
