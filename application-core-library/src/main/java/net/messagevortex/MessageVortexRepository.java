package net.messagevortex;

import net.messagevortex.accounting.Accountant;
import net.messagevortex.asn1.IdentityBlock;
import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.blender.Blender;
import net.messagevortex.router.Router;
import net.messagevortex.router.operation.InternalPayloadSpace;
import net.messagevortex.router.operation.InternalPayloadSpaceStore;
import net.messagevortex.transport.Transport;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageVortexRepository {

  private static class Store {
    public final Map<String, Transport> transport = new ConcurrentHashMap<>();
    public final Map<String, Blender> blender = new ConcurrentHashMap<>();
    public final Map<String, Router> router = new ConcurrentHashMap<>();
    public final Map<String, Accountant> accountant = new ConcurrentHashMap<>();
    public final Map<String, IdentityStore> identityStore = new ConcurrentHashMap<>();
    public final InternalPayloadSpaceStore ownStores = new InternalPayloadSpaceStore();
    public final InternalPayloadSpaceStore simStores = new InternalPayloadSpaceStore();
  }

  private static final Map<String, Store> store = new ConcurrentHashMap<>();

  /**
   * <p>Set the transport specified in the named configuration section.</p>
   *
   * @param uid the UUID of the related store
   * @param id the name of the config section
   * @param a the transport handler
   */
  public static void setTransport(String uid, String id, Transport a) {
    Store rstore = store.get(uid);
    if (rstore == null) {
      rstore = new Store();
      store.put(uid, rstore);
    }
    rstore.transport.put(id.toLowerCase(), a);
  }

  /**
   * <p>Get the accountant specified in the named configuration section.</p>
   *
   * @param uid the UUID of the related store
   * @param id  the name of the config section
   * @return the requested accountant or null
   */
  public static Accountant getAccountant(String uid, String id) {
    Store rstore = store.get(uid);
    if (rstore == null || id == null) {
      return null;
    }
    return rstore.accountant.get(id.toLowerCase());
  }

  /**
   * <p>Set the accountant specified in the named configuration section.</p>
   *
   * @param uid the UUID of the related store
   * @param id the name of the config section
   * @param a the accounting handler
   */
  public static void setAccountant(String uid, String id, Accountant a) {
    Store rstore = store.get(uid);
    if (rstore == null) {
      rstore = new Store();
      store.put(uid, rstore);
    }
    rstore.accountant.put(id.toLowerCase(), a);
  }

  /***
   * <p>Get the blender specified in the named configuration section.</p>
   *
   * @param uid the UUID of the related store
   * @param id the name of the config section
   * @return the requested blender or null
   */
  public static Blender getBlender(String uid, String id) {
    Store rstore = store.get(uid);
    if (rstore == null || id == null) {
      return null;
    }
    return rstore.blender.get(id.toLowerCase());
  }

  /**
   * <p>Set the blender specified in the named configuration section.</p>
   *
   * @param uid the UUID of the related store
   * @param id the name of the config section
   * @param a the blending handler
   */
  public static void setBlender(String uid, String id, Blender a) {
    Store rstore = store.get(uid);
    if (rstore == null) {
      rstore = new Store();
      store.put(uid, rstore);
    }
    rstore.blender.put(id.toLowerCase(), a);
  }

  /***
   * <p>Get the router specified in the named configuration section.</p>
   *
   * @param uid the UUID of the related store
   * @param id the name of the config section
   * @return the requested router or null
   */
  public static Router getRouter(String uid, String id) {
    Store rstore = store.get(uid);
    if (rstore == null || id == null) {
      return null;
    }
    return rstore.router.get(id.toLowerCase());
  }

  /**
   * <p>Set the router specified in the named configuration section.</p>
   *
   * @param uid the UUID of the related store
   * @param id the name of the config section
   * @param a the routing handler
   */
  public static void setRouter(String uid, String id, Router a) {
    Store rstore = store.get(uid);
    if (rstore == null) {
      rstore = new Store();
      store.put(uid, rstore);
    }
    rstore.router.put(id.toLowerCase(), a);
  }

  /***
   * <p>Get the identity store specified in the named configuration section.</p>
   *
   * @param uid the UUID of the related store
   * @param id the name of the config section
   * @return the requested identity store or null
   */
  public static IdentityStore getIdentityStore(String uid, String id) {
    Store rstore = store.get(uid);
    if (rstore == null || id == null) {
      return null;
    }
    return rstore.identityStore.get(id.toLowerCase());
  }

  /***
   * <p>Set the identity store specified in the named configuration section.</p>
   *
   * @param uid the UUID of the related store
   * @param id the name of the config section
   * @param is the identity store
   */
  public static void setIdentityStore(String uid, String id, IdentityStore is) {
    Store rstore = store.get(uid);
    if (rstore == null) {
      rstore = new Store();
      store.put(uid, rstore);
    }
    rstore.identityStore.put(id.toLowerCase(), is);
  }

  /***
   * <p>gets a simulated payload space for a specific identity block.</p>
   *
   * @param uid the UUID of the related store
   * @param ib the identity block
   * @return the requested payload space
   */
  public static InternalPayloadSpace getSimulatedSpace(String uid, IdentityBlock ib) {
    Store rstore = store.get(uid);
    if (rstore == null) {
      return null;
    }
    // get exiting space
    return rstore.simStores.getInternalPayload(ib);
  }

  /***
   * <p>Gets own payload space for a specific identity.</p>
   *
   * @param uid the UUID of the related store
   * @param ib the identityblock identifying the payload space
   * @return the requested payload space
   */
  public static InternalPayloadSpace getOwnSpace(String uid, IdentityBlock ib) {
    Store rstore = store.get(uid);
    if (rstore == null) {
      return null;
    }
    return rstore.ownStores.getInternalPayload(ib);
  }

  /**
   * <p>Remove an id from the space.</p>
   *
   * @param uid the uid to be removed
   */
  public static void clear(String uid) {
    store.remove(uid);
  }

  /**
   * <p>Get a list of all running daemons.</p>
   *
   * @param uid the affected uid
   * @return the requested list
   */
  public static Map<String,RunningDaemon> getRunningDaemons(String uid) {
    Store rstore = store.get(uid);
    if (rstore == null) {
      return null;
    }
    Map<String,RunningDaemon> ret=new HashMap<>();
    ret.putAll(rstore.transport);
    ret.putAll(rstore.blender);
    ret.putAll(rstore.router);
    ret.putAll(rstore.accountant);
    return ret;
  }
}
