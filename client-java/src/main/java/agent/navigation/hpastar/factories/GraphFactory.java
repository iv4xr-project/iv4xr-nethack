//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package agent.navigation.hpastar.factories;

import agent.navigation.hpastar.Cluster;
import agent.navigation.hpastar.IPassability;
import agent.navigation.hpastar.Size;
import agent.navigation.hpastar.TileType;
import agent.navigation.hpastar.graph.ConcreteEdgeInfo;
import agent.navigation.hpastar.graph.ConcreteGraph;
import agent.navigation.hpastar.graph.ConcreteNode;
import agent.navigation.hpastar.graph.ConcreteNodeInfo;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.utils.RefSupport;
import agent.navigation.strategy.NavUtils;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.List;

public class GraphFactory {
  public static ConcreteGraph createGraph(Size size, IPassability passability) {
    ConcreteGraph graph = new ConcreteGraph();
    createNodes(size, graph, passability);
    createEdges(size, graph, TileType.OctileUnicost, passability);
    return graph;
  }

  // We hardcode OCTILE for the time being
  public static ConcreteNode getNodeByPos(ConcreteGraph graph, int x, int y, int width) {
    return graph.getNode(getNodeIdFromPos(x, y, width));
  }

  public static Id<ConcreteNode> getNodeIdFromPos(int left, int top, int width) {
    return new Id<ConcreteNode>().from(top * width + left);
  }

  public static Id<Cluster> getClusterIdFromPos(int left, int top, int width, int clusterSize) {
    int nrClustersPerRow = width % clusterSize == 0 ? width / clusterSize : width / clusterSize + 1;
    return new Id<Cluster>().from((top / clusterSize) * nrClustersPerRow + left / clusterSize);
  }

  public static void addEdge(
      ConcreteGraph graph, Id<ConcreteNode> nodeId, int x, int y, Size size, boolean isDiag) {
    if (y < 0 || y >= size.height || x < 0 || x >= size.width) return;

    ConcreteNode targetNode = getNodeByPos(graph, x, y, size.width);
    int cost = targetNode.info.cost;
    cost = isDiag ? (cost * 34) / 24 : cost;
    graph.addEdge(nodeId, targetNode.nodeId, new ConcreteEdgeInfo(cost));
  }

  private static void createEdges(
      Size size, ConcreteGraph graph, TileType tileType, IPassability passability) {
    for (int top = 0; top < size.height; ++top) {
      if (top == 7) {
        System.out.println();
      }
      for (int left = 0; left < size.width; ++left) {
        IntVec2D currentPos = new IntVec2D(left, top);
        if (!passability.canEnter(currentPos, new RefSupport<>())) {
          continue;
        }
        Id<ConcreteNode> nodeId =
            getNodeByPos(graph, currentPos.x, currentPos.y, size.width).nodeId;
        if (tileType == TileType.Hex) {
          if (left % 2 == 0) {
            addEdge(graph, nodeId, left + 1, top - 1, size, false);
            addEdge(graph, nodeId, left - 1, top - 1, size, false);
          } else {
            addEdge(graph, nodeId, left + 1, top + 1, size, false);
            addEdge(graph, nodeId, left - 1, top + 1, size, false);
          }
          continue;
        }

        List<IntVec2D> neighbours = NavUtils.neighbourCoordinates(currentPos, size, true);
        for (IntVec2D neighbourPos : neighbours) {
          if (!passability.canEnter(neighbourPos, new RefSupport<>())) {
            continue;
          }
          boolean isDiagonal = NavUtils.isDiagonal(currentPos, neighbourPos);
          if (isDiagonal && !passability.canMoveDiagonal(currentPos, neighbourPos)) {
            continue;
          }
          addEdge(graph, nodeId, neighbourPos.x, neighbourPos.y, size, isDiagonal);
        }
      }
    }
  }

  private static void createNodes(Size size, ConcreteGraph graph, IPassability passability) {
    for (int top = 0; top < size.height; ++top) {
      for (int left = 0; left < size.width; ++left) {
        Id<ConcreteNode> nodeId = getNodeIdFromPos(left, top, size.width);
        IntVec2D position = new IntVec2D(left, top);
        RefSupport<Integer> refVar___0 = new RefSupport<>();
        boolean isObstacle = !passability.canEnter(position, refVar___0);
        int movementCost = refVar___0.getValue();
        ConcreteNodeInfo info = new ConcreteNodeInfo(isObstacle, movementCost, position);
        graph.addNode(nodeId, info);
      }
    }
  }
}
