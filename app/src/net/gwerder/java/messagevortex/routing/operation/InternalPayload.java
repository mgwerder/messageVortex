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

import net.gwerder.java.messagevortex.MessageVortexLogger;
import net.gwerder.java.messagevortex.asn1.IdentityBlock;
import net.gwerder.java.messagevortex.asn1.PayloadChunk;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Represents a payload space of an identity in memory for processing
 */
public class InternalPayload {

    private static final java.util.logging.Logger LOGGER;
    static {
        LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
        MessageVortexLogger.setGlobalLogLevel( Level.ALL);
    }


    InternalPayloadSpace payloadSpace;
    IdentityBlock identity;
    final List<Operation> operations=new ArrayList<>();
    final Map<Integer,PayloadChunk> internalPayload=new ConcurrentHashMap<>();
    final Map<Integer,PayloadChunk> internalPayloadCache=new ConcurrentHashMap<>();
    final Map<Integer,Operation> internalOperationOutput=new ConcurrentHashMap<>();
    final Map<Integer,List<Operation>> internalOperationInput=new ConcurrentHashMap<>();

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
            if(pc!=null) {
                return pc;
            }

            // Check if already in payload cache
            pc=getPayloadCache(id);
            if(pc!=null) {
                return pc;
            }

            // build if payload cache is empty
            Operation op=internalOperationOutput.get(id);
            if(op!=null && op.canRun()) {
                LOGGER.log(Level.INFO,"executing operation "+op+" of identity "+getIdentity()+" to populate payload id "+id);
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
            // invalidate all cached payloads depending on this value
            invalidateInternalPayloadCache(id);

            return old;
        }
    }

    private void invalidateInternalPayloadCache(int id) {
        // WARNING this method is not as threadsafe as it should be

        List<Operation> ops=new ArrayList<>();
        // remove calculated value of field
        setCalculatedPayload(id,null);

        //invalidate all subsequent depending values
        synchronized (operations) {
            for(Operation op:operations) {
                if(Arrays.binarySearch(op.getInputID(),id)>=0) {
                    for(int i:op.getOutputID()) {
                        invalidateInternalPayloadCache(i);
                    }
                }
            }
        }
        internalPayloadCache.clear();
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
        // check for valid operation
        if(op==null || op.getOutputID()==null || op.getOutputID().length==0) {
            throw new NullPointerException();
        }

        // search for circular dependencies
        for(int id:op.getOutputID()) {
            if(isCircularDependent(op,id)) {
                throw new InvalidParameterException("circular dependency detected on id "+id);
            }
        }

        // register output ids
        int[] id=op.getOutputID();
        for(int i=0;i<id.length;i++) {
            internalOperationOutput.put(id[i],op);
        }

        //register input ids
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

        // register operation
        operations.add(op);
    }

    private boolean isCircularDependent(Operation op,int id) {
        Operation top=internalOperationOutput.get(id);
        if (top != null) {
            for (int tid : top.getInputID()) {
                // this operation generates that id
                if (Arrays.binarySearch(op.getOutputID(), tid) >= 0) {
                    return true;
                } else if (isCircularDependent(op, tid)) {
                    return true;
                }
            }
        }
        return false;
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
        synchronized(internalOperationInput) {
            for(int i=0;i<id.length;i++) {
                List<Operation> l=internalOperationInput.get(id[i]);
                l.remove(op);
                if(l!=null && l.size()==0) {
                    internalOperationInput.remove(id[i]);
                }
            }
        }
        operations.remove(op);
    }

    public boolean addOperation(Operation op) {
        // do first a compact cycle if required
        compact();

        // check for conflicting operations
        for( int id:op.getOutputID()) {
            if(internalOperationOutput.get(id)!=null) {
                return false;
            }
        }

        // store operation
        registerOperation(op);

        return true;
    }

    public boolean removeOperation(Operation op) {
        // check for conflicting operations
        for( int id:op.getOutputID()) {
            if(internalOperationOutput.get(id)!=null) {
                return false;
            }
        }

        // store operation
        deregisterOperation(op);

        // do first a compact cycle if required
        compact();

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
                if(! op.isInUsagePeriod()) {
                    ops.add(op);
                }
            }
            for(Operation op:ops) {
                LOGGER.log(Level.INFO,"clearing expired operation "+op+" of identity "+getIdentity());
                deregisterOperation(op);
            }
        }

        // remove expired payloads
        synchronized(internalPayload) {

            // search for expired payloads
            List<Integer> expiredPayloadIds=new ArrayList<>();
            for(Map.Entry<Integer,PayloadChunk> pce:internalPayload.entrySet()) {
                if(!pce.getValue().isInUsagePeriod()) {
                    expiredPayloadIds.add(pce.getKey());
                }
            }

            // remove expired payloads
            for(int i:expiredPayloadIds) {
                LOGGER.log(Level.INFO,"clearing expired payload "+i+" of identity "+getIdentity());
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
