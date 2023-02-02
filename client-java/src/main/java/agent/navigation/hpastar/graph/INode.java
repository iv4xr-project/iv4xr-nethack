//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.graph;

import agent.navigation.hpastar.infrastructure.Id;
import java.util.HashMap;
import java.util.Map;

public abstract class INode<TId, TInfo, TEdge> {
  public Id<TId> nodeId;
  public TInfo info;
  public Map<Id<TId>, TEdge> edges = new HashMap<>();

  public abstract void removeEdge(Id<TId> targetNodeId);

  public abstract void addEdge(TEdge targetNodeId);
}
