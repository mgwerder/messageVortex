package net.messagevortex.router;

import net.messagevortex.AbstractDaemon;
import net.messagevortex.MessageVortexRepository;
import net.messagevortex.accounting.Accountant;
import net.messagevortex.asn1.VortexMessage;
import net.messagevortex.blender.Blender;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleRouterImplementation extends AbstractDaemon implements Router {

  private Accountant accountant;
  private final Map<String, Blender> blenders = new ConcurrentHashMap<>();

  public SimpleRouterImplementation(String section) {
    // get accounting layer
    setAccountant(MessageVortexRepository.getAccountant("", section));
  }

  @Override
  public boolean addBlendingLayer(Blender blendingLayer) {
    String id = blendingLayer.getBlendingAddress();
    blendingLayer.setBlenderReceiver(this);
    blenders.put(id, blendingLayer);
    return true;
  }

  @Override
  public boolean removeBlendingLayer(Blender blendingLayer) {
    if (!blenders.containsValue(blendingLayer)) {
      return false;
    }
    synchronized (blenders) {
      Set<String> keys = new HashSet<>();
      for (Map.Entry<String, Blender> entry : blenders.entrySet()) {
        if (Objects.equals(blendingLayer, entry.getValue())) {
          keys.add(entry.getKey());
        }
      }
      for (String key : keys) {
        blenders.remove(key);
      }
    }
    return true;
  }

  @Override
  public List<Blender> getAllBlendingLayer() {
    return new ArrayList<>(blenders.values());
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
