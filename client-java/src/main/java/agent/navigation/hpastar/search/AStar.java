package agent.navigation.hpastar.search;
;
import agent.navigation.hpastar.Connection;
import agent.navigation.hpastar.infrastructure.IMap;
import agent.navigation.hpastar.infrastructure.Id;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import nethack.world.Level;
import util.Loggers;

public class AStar<TNode> {
  private final Function<Id<TNode>, Boolean> isGoal;

  private final Function<Id<TNode>, Integer> calculateHeuristic;

  private final IMap<TNode> map;

  private final PriorityQueue<Priotisable<Id<TNode>>> openQueue =
      new PriorityQueue<>(new PriotisableComperator<>());

  private final NodeLookup<TNode> nodeLookup;

  public AStar(IMap<TNode> map, Id<TNode> startNodeId, Id<TNode> targetNodeId) {
    isGoal = (Id<TNode> nodeId) -> nodeId.equals(targetNodeId);
    calculateHeuristic = (Id<TNode> nodeId) -> map.getHeuristic(nodeId, targetNodeId);
    this.map = map;
    int estimatedCost = calculateHeuristic.apply(startNodeId);
    AStarNode<TNode> startNode = new AStarNode<>(startNodeId, 0, estimatedCost, CellStatus.Open);
    openQueue.add(new Priotisable<Id<TNode>>(startNodeId, startNode.f));
    nodeLookup = new NodeLookup<TNode>(map.getNrNodes());
    nodeLookup.setNodeValue(startNodeId, startNode);
  }

  public final boolean canExpand() {
    return !openQueue.isEmpty();
  }

  public final IdPath<TNode> findPath() {
    while (canExpand()) {
      Id<TNode> nodeId = expand();
      int id = nodeId.getIdValue();
      Loggers.HPALogger.trace(
          "Expand <%d,%d> (relPos:<%d,%d>) %d",
          id % Level.SIZE.width, id / Level.SIZE.width, id % 8, id / 8, id);
      if (isGoal.apply(nodeId)) {
        Loggers.HPALogger.trace("Found path");
        openQueue.clear();
        return reconstructPathFrom(nodeId);
      }
    }

    // If no path is found, don't return magic value with path cost '-1'
    return null;
  }

  private Id<TNode> expand() {
    var nodeId = openQueue.remove();
    var node = nodeLookup.getNodeValue(nodeId.item);
    processNeighbours(nodeId.item, node);
    nodeLookup.setNodeValue(
        nodeId.item, new AStarNode<TNode>(node.parent, node.g, node.h, CellStatus.Closed));
    return nodeId.item;
  }

  private void processNeighbours(Id<TNode> nodeId, AStarNode<TNode> node) {
    Iterable<Connection<TNode>> connections = map.getConnections(nodeId);
    for (Connection<TNode> connection : connections) {
      int gCost = node.g + connection.cost;
      Id<TNode> neighbour = connection.target;
      if (nodeLookup.nodeIsVisited(neighbour)) {
        var targetAStarNode = nodeLookup.getNodeValue(neighbour);
        //  If we already processed the neighbour in the past, or was already found in the past
        //  a better path to reach this node that the current one, just skip it, else create
        //  and replace a new PathNode
        if (targetAStarNode.status == CellStatus.Closed || gCost >= targetAStarNode.g) {
          continue;
        }

        targetAStarNode = new AStarNode<TNode>(nodeId, gCost, targetAStarNode.h, CellStatus.Open);
        List<Priotisable<Id<TNode>>> items =
            openQueue.stream().filter(i -> i.item.equals(neighbour)).collect(Collectors.toList());
        assert items.size() == 1;
        Priotisable<Id<TNode>> item = items.get(0);
        openQueue.remove(item);
        openQueue.add(new Priotisable<>(item.item, targetAStarNode.f));
        nodeLookup.setNodeValue(neighbour, targetAStarNode);
      } else {
        int newHeuristic = calculateHeuristic.apply(neighbour);
        AStarNode<TNode> newAStarNode =
            new AStarNode<>(nodeId, gCost, newHeuristic, CellStatus.Open);
        openQueue.add(new Priotisable<>(neighbour, newAStarNode.f));
        nodeLookup.setNodeValue(neighbour, newAStarNode);
      }
    }
  }

  ///  <summary>
  ///  Reconstructs the path from the destination node with the aid
  ///  of the node Lookup that stored the states of all processed nodes
  ///  TODO: Maybe I should guard this with some kind of safetyGuard to prevent
  ///  possible infinite loops in case of bugs, but meh...
  ///  </summary>
  private IdPath<TNode> reconstructPathFrom(Id<TNode> destination) {
    List<Id<TNode>> pathNodes = new ArrayList<>();
    int pathCost = nodeLookup.getNodeValue(destination).f;
    Id<TNode> currentNode = destination;
    while (nodeLookup.getNodeValue(currentNode).parent != currentNode) {
      pathNodes.add(currentNode);
      currentNode = this.nodeLookup.getNodeValue(currentNode).parent;
    }

    pathNodes.add(currentNode);
    Collections.reverse(pathNodes);
    return new IdPath<TNode>(pathNodes, pathCost);
  }

  /**
   * Wraps around a type to add a float value on which can be sorted.
   *
   * @param <T> The type to wrap around.
   */
  static class Priotisable<T> {
    public final float priority;
    public final T item;

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

  static class PriotisableComperator<T> implements Comparator<Priotisable<T>> {
    @Override
    public int compare(Priotisable<T> o1, Priotisable<T> o2) {
      return Float.compare(o1.priority, o2.priority);
    }
  }
}
