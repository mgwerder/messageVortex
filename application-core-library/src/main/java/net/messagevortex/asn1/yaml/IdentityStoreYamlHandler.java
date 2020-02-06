package net.messagevortex.asn1.yaml;

import java.util.List;
import java.util.Vector;
import net.messagevortex.asn1.IdentityStore;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;

public class IdentityStoreYamlHandler extends Constructor implements Represent {
  
  @Override
  public Node representData(Object data) {
    IdentityStore identityStore = (IdentityStore) data;
    List<NodeTuple> value = new Vector<>();
    
    return new MappingNode(new Tag("!identityStore"),value,true);
  }
  
  
}
