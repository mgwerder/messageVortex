package net.gwerder.java.messagevortex.routing.operation;
/***
 * Copyright (c) 2018 Martin Gwerder (martin@gwerder.net)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ***/


import net.gwerder.java.messagevortex.asn1.Identity;
import net.gwerder.java.messagevortex.asn1.PayloadChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a payload in memory for processing
 */
public class InternalPayload {


    InternalPayloadSpace payloadSpace;
    Identity identity;
    List<Operation> operations=new ArrayList<>();
    Map<Integer,PayloadChunk> internalPayload=new ConcurrentHashMap<>();
    Map<Integer,Operation> internalOperationOutput=new ConcurrentHashMap<>();

    private long lastcompact=System.currentTimeMillis();

    protected InternalPayload( InternalPayloadSpace payloadSpace, Identity i) {
        identity=i;
        this.payloadSpace=payloadSpace;
        payloadSpace.setPayload(i,this);
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

    private void registerOperation(Operation op) {
        if(op==null || op.getOutputID()==null) {
            throw new NullPointerException();
        }
        int[] id=op.getOutputID();
        for(int i=0;i<id.length;i++) {
            internalOperationOutput.put(id[i],op);
        }
        operations.add(op);
    }

    public boolean setOperation(Operation op) {
        // do first a compact cycle if required
        compact();

        // FIXME check for conflicting operations

        // FIXME check for valid usage Period

        // store operation
        registerOperation(op);

        return true;
    }

    protected boolean compact() {
        // skip running if last run is less than 60s ago
        if(System.currentTimeMillis()<lastcompact+60000) {
            return false;
        }

        // FIXME compact structure

        // FIXME remove expired identities

        // FIXME remove expired operations

        // FIXME remove expired payloads

        return true;
    }

}
