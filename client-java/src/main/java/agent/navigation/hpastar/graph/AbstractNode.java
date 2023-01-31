//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.graph;

import agent.navigation.hpastar.infrastructure.Id;
import java.util.HashMap;
import java.util.Map;

public class AbstractNode extends INode<AbstractNode, AbstractNodeInfo, AbstractEdge> {
  public Id<AbstractNode> nodeId;
  public AbstractNodeInfo info;
  public Map<Id<AbstractNode>, AbstractEdge> edges = new HashMap<>();

  public AbstractNode(Id<AbstractNode> nodeId, AbstractNodeInfo info) {
    this.nodeId = nodeId;
    this.info = info;
  }

  public void removeEdge(Id<AbstractNode> targetNodeId) {
    edges.remove(targetNodeId);
  }

  public void addEdge(AbstractEdge edge) {
    if (!edges.containsKey(edge.targetNodeId)
        || edges.get(edge.targetNodeId).info.level < edge.info.level) {
      edges.put(edge.targetNodeId, edge);
    }
  }
}
