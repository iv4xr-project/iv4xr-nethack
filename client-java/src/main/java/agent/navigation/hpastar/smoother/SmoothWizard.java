//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:35
//

package agent.navigation.hpastar.smoother;

import agent.navigation.hpastar.ConcreteMap;
import agent.navigation.hpastar.ConcretePathNode;
import agent.navigation.hpastar.IPathNode;
import agent.navigation.hpastar.TileType;
import agent.navigation.hpastar.graph.ConcreteGraph;
import agent.navigation.hpastar.graph.ConcreteNode;
import agent.navigation.hpastar.graph.ConcreteNodeInfo;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.search.AStar;
import agent.navigation.hpastar.search.Path;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import nl.uu.cs.aplib.utils.Pair;

public class SmoothWizard {
  private List<IPathNode> initialPath = new ArrayList<>();
  private static final Id<ConcreteNode> INVALID_ID = new Id<ConcreteNode>().from(Constants.NO_NODE);
  private final ConcreteMap _concreteMap;
  // This is a map, indexed by nodeId, that tells in which order does this node occupy in the path
  private final Map<Integer, Integer> _pathMap = new HashMap<>();

  public SmoothWizard(ConcreteMap concreteMap, List<IPathNode> path) {
    initialPath = path;
    _concreteMap = concreteMap;
    for (int i = 0; i < initialPath.size(); i++) {
      _pathMap.put(initialPath.get(i).getIdValue(), i + 1);
    }
  }

  private IntVec2D getPosition(Id<ConcreteNode> nodeId) {
    return _concreteMap.graph.getNodeInfo(nodeId).position;
  }

  public List<IPathNode> smoothPath() {
    List<ConcretePathNode> smoothedConcretePath = new ArrayList<>();
    for (int index = 0;
        index < initialPath.size() && initialPath.get(index) instanceof ConcretePathNode;
        index++) {
      ConcretePathNode pathNode = (ConcretePathNode) initialPath.get(index);
      if (smoothedConcretePath.isEmpty()) {
        smoothedConcretePath.add(pathNode);
      }
      // add this node to the smoothed path
      if (!smoothedConcretePath.get(smoothedConcretePath.size() - 1).id.equals(pathNode.id)) {
        // It's possible that, when smoothing, the next node that will be put in the path
        // will not be adjacent. In those cases, since OpenRA requires a continuous path
        // without breaking, we should calculate a new path for that section
        ConcretePathNode lastNodeInSmoothedPath =
            smoothedConcretePath.get(smoothedConcretePath.size() - 1);
        if (!areAdjacent(getPosition(lastNodeInSmoothedPath.id), getPosition(pathNode.id))) {
          List<Id<ConcreteNode>> intermediatePath =
              generateIntermediateNodes(
                  smoothedConcretePath.get(smoothedConcretePath.size() - 1).id, pathNode.id);
          for (int i = 1; i < intermediatePath.size(); i++) {
            smoothedConcretePath.add(new ConcretePathNode(intermediatePath.get(i)));
          }
        } else {
          smoothedConcretePath.add(pathNode);
        }
      }

      index = decideNextNodeToConsider(index);
    }
    return new ArrayList<>(smoothedConcretePath);
  }

  private int decideNextNodeToConsider(int index) {
    int newIndex = index;
    for (int dir = ((Enum) Direction.North).ordinal();
        dir <= ((Enum) Direction.NorthWest).ordinal();
        dir++) {
      if (_concreteMap.tileType == TileType.Tile && dir > ((Enum) Direction.West).ordinal()) break;

      Id<ConcreteNode> seenPathNode =
          advanceThroughDirection(
              new Id<ConcreteNode>().from(initialPath.get(index).getIdValue()), dir);
      if (seenPathNode == INVALID_ID) continue;

      // No node in advance in that direction, just continue
      if (index > 0 && seenPathNode.getIdValue() == initialPath.get(index - 1).getIdValue())
        continue;

      // If the point we are advancing is the same as the previous one, we didn't
      // improve at all. Just continue looking other directions
      if (index < initialPath.size() - 1
          && seenPathNode.getIdValue() == initialPath.get(index + 1).getIdValue()) continue;

      // If the point we are advancing is the same as a next node in the path,
      // we didn't improve either. Continue next direction
      newIndex = _pathMap.get(seenPathNode.getIdValue()) - 2;
      break;
    }
    return newIndex;
  }

  // count the path reduction (e.g., 2)
  private static boolean areAdjacent(IntVec2D a, IntVec2D b) {
    return Math.abs(a.x - b.x) <= 1 && Math.abs(a.y - b.y) <= 1;
  }

  // if the Manhattan distance between a and b is > 2, then they are not
  // (At least on OCTILE)
  private List<Id<ConcreteNode>> generateIntermediateNodes(
      Id<ConcreteNode> nodeid1, Id<ConcreteNode> nodeid2) {
    AStar<ConcreteNode> search = new AStar<>(_concreteMap, nodeid1, nodeid2);
    Path<ConcreteNode> path = search.findPath();
    return path.pathNodes;
  }

  /**
   * Returns the next node in the init path in a straight line that lies in the same direction as
   * the origin node
   */
  private Id<ConcreteNode> advanceThroughDirection(Id<ConcreteNode> originId, int direction) {
    Id<ConcreteNode> nodeId = originId;
    Id<ConcreteNode> lastNodeId = originId;
    while (true) {
      // advance in the given direction
      nodeId = advanceNode(nodeId, direction);
      // If in the direction we advanced there was an invalid node or we cannot enter the node,
      // just return that no node was found
      if (nodeId == INVALID_ID
          || !_concreteMap.canJump(getPosition(nodeId), getPosition(lastNodeId))) return INVALID_ID;

      // Otherwise, if the node we advanced was contained in the original path, and
      // it was positioned after the node we are analyzing, return it
      if (_pathMap.containsKey(nodeId.getIdValue())
          && _pathMap.get(nodeId.getIdValue()) > _pathMap.get(originId.getIdValue())) {
        return nodeId;
      }

      // If we have found an obstacle, just return that no next node to advance was found
      ConcreteNodeInfo newNodeInfo = _concreteMap.graph.getNodeInfo(nodeId);
      if (newNodeInfo.isObstacle) {
        return INVALID_ID;
      }

      lastNodeId = nodeId;
    }
  }

  private Id<ConcreteNode> advanceNode(Id<ConcreteNode> nodeId, int direction) {
    ConcreteNodeInfo nodeInfo = _concreteMap.graph.getNodeInfo(nodeId);
    int y = nodeInfo.position.y;
    int x = nodeInfo.position.x;
    ConcreteGraph tilingGraph = _concreteMap.graph;
    Function<Pair<Integer, Integer>, ConcreteNode> getNode =
        (coords) -> {
          return tilingGraph.getNode(_concreteMap.getNodeIdFromPos(coords.fst, coords.snd));
        };
    switch (Direction.values()[direction]) {
      case North:
        if (y == 0) return INVALID_ID;

        return getNode.apply(new Pair<>(x, y - 1)).nodeId;
      case East:
        if (x == _concreteMap.size.width - 1) return INVALID_ID;

        return getNode.apply(new Pair<>(x + 1, y)).nodeId;
      case South:
        if (y == _concreteMap.size.height - 1) return INVALID_ID;

        return getNode.apply(new Pair<>(x, y + 1)).nodeId;
      case West:
        if (x == 0) return INVALID_ID;

        return getNode.apply(new Pair<>(x - 1, y)).nodeId;
      case NorthEast:
        if (y == 0 || x == _concreteMap.size.width - 1) return INVALID_ID;

        return getNode.apply(new Pair<>(x + 1, y - 1)).nodeId;
      case SouthEast:
        if (y == _concreteMap.size.height - 1 || x == _concreteMap.size.width - 1)
          return INVALID_ID;

        return getNode.apply(new Pair<>(x + 1, y + 1)).nodeId;
      case SouthWest:
        if (y == _concreteMap.size.height - 1 || x == 0) return INVALID_ID;

        return getNode.apply(new Pair<>(x - 1, y + 1)).nodeId;
      case NorthWest:
        if (y == 0 || x == 0) return INVALID_ID;

        return getNode.apply(new Pair<>(x - 1, y - 1)).nodeId;
      default:
        return INVALID_ID;
    }
  }
}
