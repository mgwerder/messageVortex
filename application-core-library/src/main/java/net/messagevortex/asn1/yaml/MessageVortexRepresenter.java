package net.messagevortex.asn1.yaml;

import net.messagevortex.asn1.IdentityStore;
import org.yaml.snakeyaml.representer.Representer;

public class MessageVortexRepresenter extends Representer {
  
  public MessageVortexRepresenter() {
    this.representers.put( IdentityStore.class, new IdentityStoreYamlHandler() );
  }
}
