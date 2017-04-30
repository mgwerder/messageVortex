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
