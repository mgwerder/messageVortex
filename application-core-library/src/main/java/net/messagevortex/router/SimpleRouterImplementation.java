package net.messagevortex.router;

import java.util.List;

import net.messagevortex.AbstractDaemon;
import net.messagevortex.MessageVortex;
import net.messagevortex.accounting.Accountant;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.blender.Blender;

public class SimpleRouterImplementation extends AbstractDaemon implements Router {

  private Accountant accountant;

  public SimpleRouterImplementation(String section) {
    // get accounting layer
    setAccountant(MessageVortex.getAccountant(section));
  }

  @Override
  public boolean addBlendingLayer(Blender blendingLayer) {
    return false;
  }

  @Override
  public boolean removeBlendingLayer(Blender blendingLayer) {
    return false;
  }

  @Override
  public List<Blender> getAllBlendingLayer() {
    return null;
  }

  @Override
  public final Accountant setAccountant(Accountant accountant) {
    Accountant ret = getAccountant();
    this.accountant = accountant;
    return ret;
  }

  @Override
  public Accountant getAccountant() {
    return accountant;
  }

  @Override
  public boolean gotMessage(VortexMessage message) {
    return false;
  }
}
