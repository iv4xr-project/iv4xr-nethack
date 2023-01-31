//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.graph;

import agent.navigation.hpastar.infrastructure.Id;

public abstract class IEdge<TNode, TEdgeInfo> {
  public Id<TNode> targetNodeId;
  public TEdgeInfo info;
}
