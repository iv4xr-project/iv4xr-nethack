//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.graph;

import agent.navigation.hpastar.infrastructure.Id;

public class ConcreteEdge extends IEdge<ConcreteNode, ConcreteEdgeInfo> {
  public ConcreteEdge(Id<ConcreteNode> targetNodeId, ConcreteEdgeInfo info) {
    this.targetNodeId = targetNodeId;
    this.info = info;
  }
}
