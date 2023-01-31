package agent.navigation.hpastar.search;

import agent.navigation.hpastar.infrastructure.Id;

public class NodeLookup<TNode> {
  private AStarNode<TNode>[] _astarNodes;

  public NodeLookup(int numberOfNodes) {
    _astarNodes = new AstarNode<TNode>[numberOfNodes];
  }

  public final void setNodeValue(Id<TNode> nodeId, AStarNode<TNode> value) {
    _astarNodes[nodeId.IdValue] = value;
  }

  public final boolean nodeIsVisited(Id<TNode> nodeId) {
    return _astarNodes[nodeId.IdValue].HasValue;
  }

  public final AStarNode<TNode> getNodeValue(Id<TNode> nodeId) {
    return _astarNodes[nodeId.IdValue].Value;
  }
}
