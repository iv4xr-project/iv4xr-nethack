package agent.navigation.hpastar.search;

import agent.navigation.hpastar.infrastructure.Id;
import java.util.ArrayList;
import java.util.List;

public class NodeLookup<TNode> {
  private final List<AStarNode<TNode>> astarNodes;

  public NodeLookup(int numberOfNodes) {
    astarNodes = new ArrayList<>(numberOfNodes);
    for (int i = 0; i < numberOfNodes; i++) {
      astarNodes.add(null);
    }
  }

  public final void setNodeValue(Id<TNode> nodeId, AStarNode<TNode> value) {
    astarNodes.set(nodeId.getIdValue(), value);
  }

  public final boolean nodeIsVisited(Id<TNode> nodeId) {
    return astarNodes.get(nodeId.getIdValue()) != null;
  }

  public final AStarNode<TNode> getNodeValue(Id<TNode> nodeId) {
    return astarNodes.get(nodeId.getIdValue());
  }
}
