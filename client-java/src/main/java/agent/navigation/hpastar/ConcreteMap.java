//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package agent.navigation.hpastar;

import agent.navigation.hpastar.factories.GraphFactory;
import agent.navigation.hpastar.graph.ConcreteEdge;
import agent.navigation.hpastar.graph.ConcreteGraph;
import agent.navigation.hpastar.graph.ConcreteNode;
import agent.navigation.hpastar.graph.ConcreteNodeInfo;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.infrastructure.IMap;
import agent.navigation.hpastar.infrastructure.Id;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.ArrayList;
import java.util.List;

public class ConcreteMap implements IMap<ConcreteNode> {
  public IPassability passability;
  public TileType tileType = TileType.Hex;

  public int height;
  public int width;
  public int maxEdges;
  public ConcreteGraph graph;

  public int getNrNodes() {
    return height * width;
  }

  public ConcreteMap(TileType tileType, int width, int height, IPassability passability) {
    this.passability = passability;
    this.tileType = tileType;
    this.maxEdges = Helpers.getMaxEdges(tileType);
    this.height = height;
    this.width = width;
    this.graph = GraphFactory.createGraph(width, height, passability);
  }

  // Create a new concreteMap as a copy of another concreteMap (just copying obstacles)
  public ConcreteMap slice(
      int horizOrigin, int vertOrigin, int width, int height, IPassability passability) {
    ConcreteMap slicedConcreteMap = new ConcreteMap(tileType, width, height, passability);
    for (ConcreteNode slicedMapNode : slicedConcreteMap.graph.nodes) {
      ConcreteNode globalConcreteNode =
          graph.getNode(
              getNodeIdFromPos(
                  horizOrigin + slicedMapNode.info.position.x,
                  vertOrigin + slicedMapNode.info.position.y));
      slicedMapNode.info.isObstacle = globalConcreteNode.info.isObstacle;
      slicedMapNode.info.cost = globalConcreteNode.info.cost;
    }
    return slicedConcreteMap;
  }

  public Id<ConcreteNode> getNodeIdFromPos(int x, int y) {
    return new Id<ConcreteNode>().from(y * width + x);
  }

  public int getHeuristic(Id<ConcreteNode> startNodeId, Id<ConcreteNode> targetNodeId) {
    IntVec2D startPosition = graph.getNodeInfo(startNodeId).position;
    IntVec2D targetPosition = graph.getNodeInfo(targetNodeId).position;
    int startX = startPosition.x;
    int targetX = targetPosition.x;
    int startY = startPosition.y;
    int targetY = targetPosition.y;
    int diffX = Math.abs(targetX - startX);
    int diffY = Math.abs(targetY - startY);
    switch (tileType) {
      case Hex:
        {
          // Vancouver distance
          // See P.Yap: Grid-based Path-Finding (LNAI 2338 pp.44-55)
          int correction = 0;
          if (diffX % 2 != 0) {
            if (targetY < startY) correction = targetX % 2;
            else if (targetY > startY) correction = startX % 2;
          }

          // TODO: Check formula is indeed wrong
          // Note: formula in paper is wrong, corrected below.
          return Math.max(0, diffY - diffX / 2 - correction) + diffX;
        }
      case OctileUnicost:
        return Math.max(diffX, diffY) * Constants.COST_ONE;
      case Octile:
        int maxDiff;
        int minDiff;
        if (diffX > diffY) {
          maxDiff = diffX;
          minDiff = diffY;
        } else {
          maxDiff = diffY;
          minDiff = diffX;
        }
        return (minDiff * Constants.COST_ONE * 34) / 24 + (maxDiff - minDiff) * Constants.COST_ONE;
      case Tile:
        return (diffX + diffY) * Constants.COST_ONE;
      default:
        return 0;
    }
  }

  public Iterable<Connection<ConcreteNode>> getConnections(Id<ConcreteNode> nodeId) {
    List<Connection<ConcreteNode>> result = new ArrayList<>();
    ConcreteNode node = graph.getNode(nodeId);
    ConcreteNodeInfo nodeInfo = node.info;
    for (ConcreteEdge edge : node.edges.values()) {
      Id<ConcreteNode> targetNodeId = edge.targetNodeId;
      ConcreteNodeInfo targetNodeInfo = graph.getNodeInfo(targetNodeId);
      if (canJump(targetNodeInfo.position, nodeInfo.position) && !targetNodeInfo.isObstacle) {
        result.add(new Connection<ConcreteNode>(targetNodeId, edge.info.cost));
      }
    }
    return result;
  }

  /**
   * Tells whether we can move from p1 to p2 in line. Bear in mind this function does not consider
   * intermediate points (it is assumed you can jump between intermediate points)
   */
  public boolean canJump(IntVec2D p1, IntVec2D p2) {
    if (tileType != TileType.Octile && tileType != TileType.OctileUnicost) return true;

    if (Helpers.areAligned(p1, p2)) return true;

    // The following piece of code existed in the original implementation.
    // It basically checks that you do not forcefully cross a blocked diagonal.
    // Honestly, this is weird, bad designed and supposes that each position is adjacent to each
    // other.
    ConcreteNodeInfo nodeInfo12 = graph.getNode(getNodeIdFromPos(p2.x, p1.y)).info;
    ConcreteNodeInfo nodeInfo21 = graph.getNode(getNodeIdFromPos(p1.x, p2.y)).info;
    return !(nodeInfo12.isObstacle && nodeInfo21.isObstacle);
  }

  private List<Character> getCharVector() {
    List<Character> result = new ArrayList<>();
    int numberNodes = getNrNodes();
    for (int i = 0; i < numberNodes; ++i)
      result.add(graph.getNodeInfo(new Id<ConcreteNode>().from(i)).isObstacle ? '@' : '.');
    return result;
  }

  public void printFormatted() {
    printFormattedChars(getCharVector());
  }

  private void printFormattedChars(List<Character> chars) {
    for (int y = 0; y < height; ++y) {
      for (int x = 0; x < width; ++x) {
        Id<ConcreteNode> nodeId = this.getNodeIdFromPos(x, y);
        System.out.println(chars.get(nodeId.getIdValue()));
      }
      System.out.println();
    }
  }

  public void printFormatted(List<Integer> path) {
    List<Character> chars = getCharVector();
    if (!path.isEmpty()) {
      for (int i : path) {
        chars.set(i, 'x');
      }
      chars.set(path.get(0), 'T');
      chars.set(path.get(path.size() - 1), 'S');
    }

    printFormattedChars(chars);
  }
}