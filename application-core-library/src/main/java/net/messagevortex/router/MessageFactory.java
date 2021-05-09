package net.messagevortex.router;

import net.messagevortex.asn1.IdentityStore;
import net.messagevortex.asn1.IdentityStoreBlock;
import net.messagevortex.asn1.RoutingCombo;
import net.messagevortex.asn1.VortexMessage;

/**
 * <p>Factory class to build full message (anonymizing structure).</p>
 */
public abstract class MessageFactory {

  protected VortexMessage fullmsg = null;

  protected String msg = "";
  protected IdentityStoreBlock source = null;
  protected IdentityStoreBlock target = null;
  protected IdentityStoreBlock hotspot = null;
  protected IdentityStore identityStore = null;

  protected MessageFactory() {

  }


  /***
   * <p>Sets the identity store to be used for creation of the message.</p>
   *
   * @param is the identity store to be set
   * @return the previously set identity store
   */
  public IdentityStore setIdentityStore(IdentityStore is) {
    IdentityStore ret = this.identityStore;
    this.identityStore = is;
    return ret;
  }

  /***
   * <p>Gets the current message as a VortexMessage.</p>
   *
   * @return the requested message
   */
  public VortexMessage getMessage() {
    if (this.fullmsg == null) {
      build();
    }
    return this.fullmsg;
  }

  public abstract RoutingCombo build();

  public abstract GraphSet getGraph();

}
