package agent.navigation.hpastar.search;

import agent.navigation.hpastar.Connection;
import agent.navigation.hpastar.infrastructure.IMap;
import agent.navigation.hpastar.infrastructure.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Function;

public class AStar<TNode> {

  private Function<Id<TNode>, Boolean> _isGoal;

  private Function<Id<TNode>, Integer> _calculateHeuristic;

  private IMap<TNode> _map;

  private PriorityQueue<Id<TNode>> _openQueue;

  private NodeLookup<TNode> _nodeLookup;

  private AStar() {}

  public AStar(IMap<TNode> map, Id<TNode> startNodeId, Id<TNode> targetNodeId) {
    _isGoal = (Id<TNode> nodeId) -> nodeId == targetNodeId;
    _calculateHeuristic = (Id<TNode> nodeId) -> map.getHeuristic(nodeId, targetNodeId);
    this._map = map;
    var estimatedCost = _calculateHeuristic.apply(startNodeId);
    var startNode = new AStarNode<TNode>(startNodeId, 0, estimatedCost, CellStatus.Open);
    this._openQueue = new PriorityQueue<Id<TNode>>();
    this._openQueue.add(startNodeId, startNode.f); // TODO: Add with priority
    this._nodeLookup = new NodeLookup<TNode>(map.getNrNodes());
    this._nodeLookup.setNodeValue(startNodeId, startNode);
  }

  public final boolean nodeIsClosed(Id<TNode> nodeId) {
    return (this._nodeLookup.nodeIsVisited(nodeId)
        && (this._nodeLookup.getNodeValue(nodeId).status == CellStatus.Closed));
  }

  public final boolean canExpand() {
    return ((this._openQueue != null) && (this._openQueue.size() != 0));
  }

  public Path<TNode> findBidiPath(IMap<TNode> map, Id<TNode> startNodeId, Id<TNode> targetNodeId) {
    var search1 = new AStar<TNode>(map, startNodeId, targetNodeId);
    var search2 = new AStar<TNode>(map, targetNodeId, startNodeId);
    var expand = 0;
    while ((search1.canExpand() && search2.canExpand())) {
      var frontier = search1.expand();
      expand++;
      if (search2.nodeIsClosed(frontier)) {
        return this.reconstructPath(search1, search2, frontier);
      }

      frontier = search2.expand();
      expand++;
      if (search1.nodeIsClosed(frontier)) {
        return this.reconstructPath(search1, search2, frontier);
      }
    }

    return new Path<TNode>(new ArrayList<>(), -1);
  }

  private Path<TNode> reconstructPath(
      AStar<TNode> search1, AStar<TNode> search2, Id<TNode> frontier) {
    Path<TNode> halfPath1 = search1.reconstructPathFrom(frontier);
    Path<TNode> halfPath2 = search2.reconstructPathFrom(frontier);
    halfPath2.pathNodes.Reverse();
    List<Id<TNode>> p = halfPath2.pathNodes;
    if (!p.isEmpty()) {
      for (int i = 1; i < p.size(); i++) {
        halfPath1.pathNodes.add(p.get(i));
      }
    }

    return halfPath1;
  }

  public final Path<TNode> findPath() {
    while (this.canExpand()) {
      var nodeId = this.Expand();
      if (_isGoal.apply(nodeId)) {
        return this.reconstructPathFrom(nodeId);
      }
    }

    return new Path<TNode>(new ArrayList<>(), -1);
  }

  private final Id<TNode> expand() {
    var nodeId = this._openQueue.remove();
    var node = this._nodeLookup.getNodeValue(nodeId);
    this.processNeighbours(nodeId, node);
    this._nodeLookup.setNodeValue(
        nodeId, new AStarNode<TNode>(node.parent, node.g, node.h, CellStatus.Closed));
    return nodeId;
  }

  private final void processNeighbours(Id<TNode> nodeId, AStarNode<TNode> node) {
    Enum<Connection<TNode>> connections = this._map.getConnections(nodeId);
    for (Connection<TNode> connection : connections) {
      var gCost = node.g + connection.cost;
      var neighbour = connection.target;
      if (this._nodeLookup.nodeIsVisited(neighbour)) {
        var targetAStarNode = this._nodeLookup.getNodeValue(neighbour);
        //  If we already processed the neighbour in the past or we already found in the past
        //  a better path to reach this node that the current one, just skip it, else create
        //  and replace a new PathNode
        if (((targetAStarNode.status == CellStatus.Closed) || (gCost >= targetAStarNode.g))) {
          // TODO: Warning!!! continue If
        }

        targetAStarNode = new AStarNode<TNode>(nodeId, gCost, targetAStarNode.h, CellStatus.Open);
        this._openQueue.UpdatePriority(neighbour, targetAStarNode.f);
        this._nodeLookup.setNodeValue(neighbour, targetAStarNode);
      } else {
        var newHeuristic = _calculateHeuristic.apply(neighbour);
        var newAStarNode = new AStarNode<TNode>(nodeId, gCost, newHeuristic, CellStatus.Open);
        this._openQueue.add(neighbour, newAStarNode.f);
        this._nodeLookup.setNodeValue(neighbour, newAStarNode);
      }
    }
  }

  ///  <summary>
  ///  Reconstructs the path from the destination node with the aid
  ///  of the node Lookup that stored the states of all processed nodes
  ///  TODO: Maybe I should guard this with some kind of safetyGuard to prevent
  ///  possible infinite loops in case of bugs, but meh...
  ///  </summary>
  private final Path<TNode> reconstructPathFrom(Id<TNode> destination) {
    var pathNodes = new ArrayList<>();
    var pathCost = this._nodeLookup.getNodeValue(destination).f;
    var currentNode = destination;
    while ((this._nodeLookup.getNodeValue(currentNode).parent != currentNode)) {
      pathNodes.add(currentNode);
      currentNode = this._nodeLookup.getNodeValue(currentNode).parent;
    }

    pathNodes.add(currentNode);
    pathNodes.Reverse();
    return new Path<TNode>(pathNodes, pathCost);
  }
}
