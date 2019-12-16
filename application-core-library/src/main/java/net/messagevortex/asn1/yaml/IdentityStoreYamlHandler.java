package net.messagevortex.asn1.yaml;

import java.util.List;
import java.util.Vector;
import net.messagevortex.asn1.IdentityStore;
import org.snakeyaml.engine.v2.api.RepresentToNode;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.yaml.snakeyaml.constructor.Constructor;

public class IdentityStoreYamlHandler extends Constructor implements RepresentToNode {
  
  @Override
  public Node representData(Object data) {
    IdentityStore identityStore = (IdentityStore) data;
    List<NodeTuple> value = new Vector<>();
    
    return new MappingNode(new Tag("!identityStore"),value,FlowStyle.AUTO);
  }
  
  
}
