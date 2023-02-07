//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.graph;

import agent.navigation.hpastar.infrastructure.Id;

public class ConcreteNode extends INode<ConcreteNode, ConcreteNodeInfo, ConcreteEdge> {
  public ConcreteNode(Id<ConcreteNode> nodeId, ConcreteNodeInfo info) {
    this.nodeId = nodeId;
    this.info = info;
  }

  public void removeEdge(Id<ConcreteNode> targetNodeId) {
    edges.remove(targetNodeId);
  }

  public void addEdge(ConcreteEdge edge) {
    edges.put(edge.targetNodeId, edge);
    // Don't add more than 8 edges
    assert edges.size() <= 8;
  }
}
