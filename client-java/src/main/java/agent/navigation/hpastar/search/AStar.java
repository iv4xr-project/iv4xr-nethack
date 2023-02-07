package agent.navigation.hpastar.search;

import agent.navigation.hpastar.Connection;
import agent.navigation.hpastar.infrastructure.IMap;
import agent.navigation.hpastar.infrastructure.Id;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AStar<TNode> {

  private Function<Id<TNode>, Boolean> _isGoal;

  private Function<Id<TNode>, Integer> _calculateHeuristic;

  private IMap<TNode> _map;

  private final PriorityQueue<Priotisable<Id<TNode>>> _openQueue =
      new PriorityQueue<>(new PriotisableComperator<>());

  private NodeLookup<TNode> _nodeLookup;

  public AStar(IMap<TNode> map, Id<TNode> startNodeId, Id<TNode> targetNodeId) {
    _isGoal = (Id<TNode> nodeId) -> nodeId.equals(targetNodeId);
    _calculateHeuristic = (Id<TNode> nodeId) -> map.getHeuristic(nodeId, targetNodeId);
    _map = map;
    int estimatedCost = _calculateHeuristic.apply(startNodeId);
    AStarNode<TNode> startNode = new AStarNode<>(startNodeId, 0, estimatedCost, CellStatus.Open);
    _openQueue.add(new Priotisable<Id<TNode>>(startNodeId, startNode.f));
    _nodeLookup = new NodeLookup<TNode>(map.getNrNodes());
    _nodeLookup.setNodeValue(startNodeId, startNode);
  }

  public final boolean nodeIsClosed(Id<TNode> nodeId) {
    return _nodeLookup.nodeIsVisited(nodeId)
        && _nodeLookup.getNodeValue(nodeId).status == CellStatus.Closed;
  }

  public final boolean canExpand() {
    return !_openQueue.isEmpty();
  }

  public Path<TNode> findBidiPath(IMap<TNode> map, Id<TNode> startNodeId, Id<TNode> targetNodeId) {
    var search1 = new AStar<TNode>(map, startNodeId, targetNodeId);
    var search2 = new AStar<TNode>(map, targetNodeId, startNodeId);
    var expand = 0;
    while (search1.canExpand() && search2.canExpand()) {
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
    Collections.reverse(halfPath2.pathNodes);
    List<Id<TNode>> p = halfPath2.pathNodes;
    if (!p.isEmpty()) {
      for (int i = 1; i < p.size(); i++) {
        halfPath1.pathNodes.add(p.get(i));
      }
    }

    return halfPath1;
  }

  public final Path<TNode> findPath() {
    while (canExpand()) {
      Id<TNode> nodeId = expand();
      //      System.out.printf("Expand: %s%n", nodeId);
      if (_isGoal.apply(nodeId)) {
        //        System.out.printf("Found path%n");
        return reconstructPathFrom(nodeId);
      }
    }

    return new Path<TNode>(new ArrayList<>(), -1);
  }

  private final Id<TNode> expand() {
    var nodeId = _openQueue.remove();
    var node = _nodeLookup.getNodeValue(nodeId.item);
    processNeighbours(nodeId.item, node);
    _nodeLookup.setNodeValue(
        nodeId.item, new AStarNode<TNode>(node.parent, node.g, node.h, CellStatus.Closed));
    return nodeId.item;
  }

  private final void processNeighbours(Id<TNode> nodeId, AStarNode<TNode> node) {
    Iterable<Connection<TNode>> connections = _map.getConnections(nodeId);
    for (Connection<TNode> connection : connections) {
      int gCost = node.g + connection.cost;
      Id<TNode> neighbour = connection.target;
      if (_nodeLookup.nodeIsVisited(neighbour)) {
        var targetAStarNode = _nodeLookup.getNodeValue(neighbour);
        //  If we already processed the neighbour in the past or we already found in the past
        //  a better path to reach this node that the current one, just skip it, else create
        //  and replace a new PathNode
        if (targetAStarNode.status == CellStatus.Closed || gCost >= targetAStarNode.g) {
          continue;
        }

        targetAStarNode = new AStarNode<TNode>(nodeId, gCost, targetAStarNode.h, CellStatus.Open);
        List<Priotisable<Id<TNode>>> items =
            _openQueue.stream().filter(i -> i.item.equals(neighbour)).collect(Collectors.toList());
        assert items.size() == 1;
        Priotisable<Id<TNode>> item = items.get(0);
        _openQueue.remove(item);
        _openQueue.add(new Priotisable<>(item.item, targetAStarNode.f));
        _nodeLookup.setNodeValue(neighbour, targetAStarNode);
      } else {
        int newHeuristic = _calculateHeuristic.apply(neighbour);
        AStarNode<TNode> newAStarNode =
            new AStarNode<>(nodeId, gCost, newHeuristic, CellStatus.Open);
        _openQueue.add(new Priotisable<>(neighbour, newAStarNode.f));
        _nodeLookup.setNodeValue(neighbour, newAStarNode);
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
    List<Id<TNode>> pathNodes = new ArrayList<>();
    int pathCost = _nodeLookup.getNodeValue(destination).f;
    Id<TNode> currentNode = destination;
    while (_nodeLookup.getNodeValue(currentNode).parent != currentNode) {
      pathNodes.add(currentNode);
      currentNode = this._nodeLookup.getNodeValue(currentNode).parent;
    }

    pathNodes.add(currentNode);
    Collections.reverse(pathNodes);
    return new Path<TNode>(pathNodes, pathCost);
  }

  /**
   * Wraps around a type to add a float value on which can be sorted.
   *
   * @param <T> The type to wrap around.
   */
  class Priotisable<T> {
    public float priority;
    public T item;

    /**
     * Wrap around an item to add a priority on which can be sorted.
     *
     * @param item: The item to wrap around.
     * @param priority: The priority on which can be sorted.
     */
    public Priotisable(T item, float priority) {
      this.item = item;
      this.priority = priority;
    }
  }

  class PriotisableComperator<T> implements Comparator<Priotisable<T>> {
    @Override
    public int compare(Priotisable<T> o1, Priotisable<T> o2) {
      return Float.compare(o1.priority, o2.priority);
    }
  }
}
