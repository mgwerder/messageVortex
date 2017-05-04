package net.gwerder.java.messagevortex.routing.operation;
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

import net.gwerder.java.messagevortex.asn1.IdentityBlock;
import net.gwerder.java.messagevortex.asn1.PayloadChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a payload space of an identity in memory for processing
 */
public class InternalPayload {


    InternalPayloadSpace payloadSpace;
    IdentityBlock identity;
    List<Operation> operations=new ArrayList<>();
    Map<Integer,PayloadChunk> internalPayload=new ConcurrentHashMap<>();
    Map<Integer,PayloadChunk> internalPayloadCache=new ConcurrentHashMap<>();
    Map<Integer,Operation> internalOperationOutput=new ConcurrentHashMap<>();
    Map<Integer,List<Operation>> internalOperationInput=new ConcurrentHashMap<>();

    private long lastcompact=System.currentTimeMillis();

    protected InternalPayload( InternalPayloadSpace payloadSpace, IdentityBlock identity) {
        this.identity=identity;
        this.payloadSpace=payloadSpace;
        this.payloadSpace.setInternalPayload(this.identity,this);
    }

    public IdentityBlock getIdentity() {
        return identity;
    }

    private PayloadChunk getPayloadCache(int id) {
        synchronized(internalPayloadCache) {
            PayloadChunk pc=internalPayloadCache.get(id);
            return pc;
        }
    }

    public PayloadChunk getPayload(int id) {
        synchronized(internalPayload) {
            PayloadChunk pc=internalPayload.get(id);
            if(pc!=null) return pc;

            // Check if already in payload cache
            pc=getPayloadCache(id);
            if(pc!=null) return pc;

            // build if payload cache is empty
            Operation op=internalOperationOutput.get(id);
            if(op!=null && op.canRun()) {
                op.execute(new int[] {id});
            }

            // return whatever we have now
            return getPayloadCache(id);
        }
    }

    public PayloadChunk setPayload(int id,PayloadChunk p) {
        compact();
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

    public void setCalculatedPayload(int id,PayloadChunk p) {
        compact();
        if(p==null) {
            internalPayloadCache.remove(id);
        } else {
            internalPayloadCache.put(id,p);
        }
    }

    private void registerOperation(Operation op) {
        if(op==null || op.getOutputID()==null) {
            throw new NullPointerException();
        }
        int[] id=op.getOutputID();
        for(int i=0;i<id.length;i++) {
            internalOperationOutput.put(id[i],op);
        }
        id=op.getInputID();
        for(int i=0;i<id.length;i++) {
            synchronized(internalOperationInput) {
                List l=internalOperationInput.get(id[i]);
                if(l==null) {
                    l=new ArrayList<>();
                    internalOperationInput.put(id[i],l);
                }
                l.add(op);
            }
        }
        operations.add(op);
    }

    private void deregisterOperation(Operation op) {
        if(op==null || op.getOutputID()==null) {
            throw new NullPointerException();
        }
        int[] id=op.getOutputID();
        for(int i=0;i<id.length;i++) {
            internalOperationOutput.remove(id[i]);
        }
        id=op.getInputID();
        for(int i=0;i<id.length;i++) {
            synchronized(internalOperationInput) {
                List<Operation> l=internalOperationInput.get(id[i]);
                l.remove(op);
                if(l!=null && l.size()==0) {
                    internalOperationInput.remove(id[i]);
                }
            }
        }
        operations.remove(op);
    }

    public boolean setOperation(Operation op) {
        // do first a compact cycle if required
        compact();

        // check for conflicting operations
        for( int id:op.getOutputID()) {
            if(internalOperationOutput.get(id)!=null) return false;
        }

        // store operation
        registerOperation(op);

        return true;
    }

    protected boolean compact() {
        // skip running if last run is less than 10s ago
        if(System.currentTimeMillis()<lastcompact+60000) {
            return false;
        }

        // remove expired operations
        synchronized(operations) {
            List<Operation> ops=new ArrayList<>();
            for(Operation op:operations) {
                if(! op.isInUsagePeriod()) ops.add(op);
            }
            for(Operation op:ops) {
                deregisterOperation(op);
            }
        }

        // remove expired payloads
        synchronized(internalPayload) {

            // search for expired payloads
            List<Integer> expiredPayloadIds=new ArrayList<>();
            for(Map.Entry<Integer,PayloadChunk> pce:internalPayload.entrySet()) {
                if(!pce.getValue().isInUsagePeriod()) expiredPayloadIds.add(pce.getKey());
            }

            // remove expired payloads
            for(int i:expiredPayloadIds) {
                setPayload(i,null);
            }

            // remove subsequent payloadcaches
            for(int i:expiredPayloadIds) {
                List<Operation> ops=internalOperationInput.get(i);
                if(ops!=null && ops.size()>0) {
                    for(Operation op:ops) {
                        for(int j:op.getOutputID()) {
                            setCalculatedPayload(j,null);
                        }
                    }
                }
            }
        }
        return true;
    }

}
