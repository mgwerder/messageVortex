package net.messagevortex.router.operation;

import net.messagevortex.MessageVortexLogger;
import net.messagevortex.asn1.IdentityBlock;
import net.messagevortex.asn1.PayloadChunk;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * <p>Represents a payload space of an identity in memory for processing.</p>
 */
public class InternalPayloadSpace {

  private static final java.util.logging.Logger LOGGER;

  static {
    LOGGER = MessageVortexLogger.getLogger((new Throwable()).getStackTrace()[0].getClassName());
  }


  InternalPayloadSpaceStore payloadSpace;
  IdentityBlock identity;
  final Set<Operation>               operations = new HashSet<>();
  final Map<Integer, PayloadChunk>   internalPayload = new HashMap<>();
  final Map<Integer, PayloadChunk>   internalPayloadCache = new ConcurrentHashMap<>();
  final Map<Integer, Operation>      internalOperationOutput = new HashMap<>();
  final Map<Integer, Set<Operation>> internalOperationInput = new HashMap<>();

  private final long lastcompact = System.currentTimeMillis();

  /***
   * <p>Creates an internal payload space and adds it to the central directory.</p>
   *
   * @param payloadSpace   the payload space to be added
   * @param identity       the identity the payload space is assigned to
   */
  public InternalPayloadSpace(InternalPayloadSpaceStore payloadSpace, IdentityBlock identity) {
    this.identity = identity;
    this.payloadSpace = payloadSpace;
    this.payloadSpace.setInternalPayload(this.identity, this);
  }

  public IdentityBlock getIdentity() {
    return identity;
  }

  private PayloadChunk getPayloadCache(int id) {
    synchronized (internalPayload) {
      return internalPayloadCache.get(id);
    }
  }

  /***
   * <p>Gets the payload of a workspace id.</p>
   *
   * @param id the payload id to be fetched
   * @return the requested chunk or null if not found
   */
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

  /***
   * <p>Sets a payload chunk.</p>
   * @param p the payload chunk to be set
   * @return the previously set payload
   */
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

  /***
   * <p>Sets an ephemeral payload.</p>
   *
   * @param id the id of the payload
   * @param p the payload
   */
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

  /***
   * <p>Add an operation to the payload space.</p>
   *
   * @param op the operation to be added
   * @return true if successful
   */
  public boolean addOperation(Operation op) {
    // do first a compact cycle if required
    compact();

    // check for conflicting operations
    for (int id : op.getOutputId()) {
      if (internalOperationOutput.get(id) != null) {
        LOGGER.log(Level.WARNING, "addin of operation " + op + " due to conflicting outputs "
                + "(conflicting op is:" + internalOperationOutput.get(id) + ")");
        return false;
      }
    }

    // store operation
    registerOperation(op);

    return true;
  }

  /***
   * <p>Remove an operation from the workspace.</p>
   *
   * @param op the operation to be removed
   * @return true if successful
   */
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
      synchronized (internalOperationInput) {
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

}
