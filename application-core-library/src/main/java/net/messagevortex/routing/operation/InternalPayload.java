package net.messagevortex.routing.operation;

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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityBlock;
import net.messagevortex.asn1.PayloadChunk;

/**
 * <p>Represents a payload space of an identity in memory for processing.</p>
 */
public class InternalPayload {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
    MessageVortexLogger.setGlobalLogLevel(Level.ALL);
  }


  InternalPayloadSpace payloadSpace;
  IdentityBlock identity;
  final List<Operation> operations = new ArrayList<>();
  final Map<Integer, PayloadChunk> internalPayload = new ConcurrentHashMap<>();
  final Map<Integer, PayloadChunk> internalPayloadCache = new ConcurrentHashMap<>();
  final Map<Integer, Operation> internalOperationOutput = new ConcurrentHashMap<>();
  final Map<Integer, Set<Operation>> internalOperationInput = new ConcurrentHashMap<>();

  private long lastcompact = System.currentTimeMillis();

  protected InternalPayload(InternalPayloadSpace payloadSpace, IdentityBlock identity) {
    this.identity = identity;
    this.payloadSpace = payloadSpace;
    this.payloadSpace.setInternalPayload(this.identity, this);
  }

  public IdentityBlock getIdentity() {
    return identity;
  }

  private PayloadChunk getPayloadCache(int id) {
    return internalPayloadCache.get(id);
  }

  public PayloadChunk getPayload(int id) {
    synchronized (internalPayload) {
      PayloadChunk pc = internalPayload.get(id);
      if (pc != null) {
        return pc;
      }

      // Check if already in payload cache
      pc = getPayloadCache(id);
      if (pc != null) {
        return pc;
      }

      // build if payload cache is empty
      Operation op = internalOperationOutput.get(id);
      if (op != null && op.canRun()) {
        LOGGER.log(Level.INFO, "executing operation " + op + " of identity " + getIdentity()
                + " to populate payload id " + id);
        op.execute(new int[]{id});
      }

      // return whatever we have now
      return getPayloadCache(id);
    }
  }

  public PayloadChunk setPayload(PayloadChunk p) {
    compact();
    synchronized (internalPayload) {
      PayloadChunk old = getPayload(p.getId());
      if (p.getPayload() == null) {
        internalPayload.remove(p.getId());
      } else {
        internalPayload.put(p.getId(), p);
      }
      // invalidate all cached payloads depending on this value
      invalidateInternalPayloadCache(p.getId());

      return old;
    }
  }

  private void invalidateInternalPayloadCache(int id) {
    // WARNING this method is not as threadsafe as it should be

    // remove calculated value of field
    setCalculatedPayload(id, null);

    //invalidate all subsequent depending values
    synchronized (operations) {
      for (Operation op : operations) {
        if (Arrays.binarySearch(op.getInputId(), id) >= 0) {
          for (int i : op.getOutputId()) {
            invalidateInternalPayloadCache(i);
          }
        }
      }
    }
    internalPayloadCache.clear();
  }

  public void setCalculatedPayload(int id, PayloadChunk p) {
    compact();
    if (p == null) {
      internalPayloadCache.remove(id);
    } else {
      internalPayloadCache.put(id, p);
    }
  }

  /***
   * <p>registers an operation in the payload space.</p>
   *
   * @param op the operation to be registered
   * @throws InvalidParameterException if dependency is circular
   */
  private void registerOperation(Operation op) {
    // check for valid operation
    if (op == null || op.getOutputId() == null || op.getOutputId().length == 0) {
      throw new NullPointerException();
    }

    // search for circular dependencies
    for (int id : op.getOutputId()) {
      if (isCircularDependent(op, id)) {
        throw new InvalidParameterException("circular dependency detected on id " + id);
      }
    }

    // check for self dependency
    for (int id : op.getOutputId()) {
      if (Arrays.binarySearch(op.getInputId(), id) > -1) {
        throw new InvalidParameterException("circular dependency detected between the in and "
                + "outputs of this function on id " + id);
      }
    }


    synchronized (operations) {
      op.setInternalPayload(this);

      // register output ids
      int[] id = op.getOutputId();
      for (int i = 0; i < id.length; i++) {
        internalOperationOutput.put(id[i], op);
      }

      //register input ids
      id = op.getInputId();
      synchronized (internalOperationInput) {
        for (int i = 0; i < id.length; i++) {
          Set<Operation> l = internalOperationInput.get(id[i]);
          if (l == null) {
            l = new HashSet<>();
            internalOperationInput.put(id[i], l);
          }
          l.add(op);
        }
      }

      // register operation
      operations.add(op);
    }
  }


  private boolean isCircularDependent(Operation op, int id) {
    Operation top = internalOperationOutput.get(id);

    if (top == null) {
      return false;
    }

    for (int tid : top.getInputId()) {

      // this operation generates that id
      if (Arrays.binarySearch(op.getOutputId(), tid) >= 0) {
        return true;
      }

      // recurse to determine
      if (isCircularDependent(op, tid)) {
        return true;
      }

    }

    return false;
  }

  private void deregisterOperation(Operation op) {
    if (op == null || op.getOutputId() == null) {
      throw new NullPointerException();
    }
    synchronized (operations) {
      op.setInternalPayload(null);

      // remove output
      int[] id = op.getOutputId();
      for (int i = 0; i < id.length; i++) {
        internalOperationOutput.remove(id[i]);
        setCalculatedPayload(id[i], null);
      }
      // remove inputs
      id = op.getInputId();
      synchronized (internalOperationInput) {
        for (int i = 0; i < id.length; i++) {
          Set<Operation> l = internalOperationInput.get(id[i]);
          if (l != null && l.isEmpty()) {
            l.remove(op);
            if (l.isEmpty()) {
              internalOperationInput.remove(id[i]);
            }
          }
        }
      }
      operations.remove(op);
    }
  }

  public boolean addOperation(Operation op) {
    // do first a compact cycle if required
    compact();

    // check for conflicting operations
    for (int id : op.getOutputId()) {
      if (internalOperationOutput.get(id) != null) {
        LOGGER.log(Level.WARNING, "addin of operation " + op + " due to conflicting outputs "
                + "(conflicting op is:" + internalOperationOutput.get(id).toString() + ")");
        return false;
      }
    }

    // store operation
    registerOperation(op);

    return true;
  }

  public boolean removeOperation(Operation op) {
    synchronized (operations) {
      // remove operation
      if (operations.contains(op)) {
        deregisterOperation(op);
      } else {
        // removal filed as operation is not registered
        return false;
      }

      // do first a compact cycle if required
      compact();
    }
    return true;
  }

  protected boolean compact() {
    // skip running if last run is less than 10s ago
    if (System.currentTimeMillis() < lastcompact + 10000) {
      return false;
    }

    compactExpiredOperations();

    compactExpiredPayloads();

    return true;
  }

  private void compactExpiredOperations() {
    // remove expired operations
    synchronized (operations) {
      List<Operation> ops = new ArrayList<>();
      for (Operation op : operations) {
        if (!op.isInUsagePeriod()) {
          ops.add(op);
        }
      }
      for (Operation op : ops) {
        LOGGER.log(Level.INFO, "clearing expired operation " + op + " of identity "
                + getIdentity());
        deregisterOperation(op);
      }
    }
  }

  private void compactExpiredPayloads() {
    // remove expired payloads
    synchronized (internalPayload) {

      // search for expired payloads
      List<Integer> expiredPayloadIds = new ArrayList<>();
      for (Map.Entry<Integer, PayloadChunk> pce : internalPayload.entrySet()) {
        if (!pce.getValue().isInUsagePeriod()) {
          expiredPayloadIds.add(pce.getKey());
        }
      }

      // remove expired payloads
      for (int i : expiredPayloadIds) {
        LOGGER.log(Level.INFO, "clearing expired payload " + i + " of identity " + getIdentity());
        setPayload(new PayloadChunk(i, null, null));
      }

      // remove subsequent payloadcaches
      for (int i : expiredPayloadIds) {
        Set<Operation> ops = internalOperationInput.get(i);
        if (ops != null && !ops.isEmpty()) {
          for (Operation op : ops) {
            for (int j : op.getOutputId()) {
              setCalculatedPayload(j, null);
            }
          }
        }
      }
    }
  }

}
