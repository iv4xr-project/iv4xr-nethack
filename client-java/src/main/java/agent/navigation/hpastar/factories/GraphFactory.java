//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package agent.navigation.hpastar.factories;

import agent.navigation.hpastar.Cluster;
import agent.navigation.hpastar.IPassability;
import agent.navigation.hpastar.NavType;
import agent.navigation.hpastar.Size;
import agent.navigation.hpastar.graph.ConcreteEdgeInfo;
import agent.navigation.hpastar.graph.ConcreteGraph;
import agent.navigation.hpastar.graph.ConcreteNode;
import agent.navigation.hpastar.graph.ConcreteNodeInfo;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.utils.RefSupport;
import agent.navigation.strategy.NavUtils;
import java.util.List;
import util.CustomVec2D;

public class GraphFactory {
  public static ConcreteGraph createGraph(Size size, IPassability passability) {
    ConcreteGraph graph = new ConcreteGraph();
    createNodes(size, graph, passability);
    createEdges(size, graph, NavType.OctileUnicost, passability);
    return graph;
  }

  public static ConcreteNode getNodeByPos(ConcreteGraph graph, CustomVec2D pos, int width) {
    return graph.getNode(getNodeIdFromPos(pos.x, pos.y, width));
  }

  public static Id<ConcreteNode> getNodeIdFromPos(int left, int top, int width) {
    return new Id<ConcreteNode>().from(top * width + left);
  }

  public static Id<Cluster> getClusterIdFromPos(int left, int top, int width, int clusterSize) {
    int nrClustersPerRow = width % clusterSize == 0 ? width / clusterSize : width / clusterSize + 1;
    return new Id<Cluster>().from(top / clusterSize * nrClustersPerRow + left / clusterSize);
  }

  public static void addEdge(
      ConcreteGraph graph, Id<ConcreteNode> nodeId, int x, int y, Size size, boolean isDiag) {
    CustomVec2D pos = new CustomVec2D(x, y);
    assert NavUtils.withinBounds(pos, size);

    ConcreteNode targetNode = getNodeByPos(graph, pos, size.width);
    int cost = targetNode.info.cost;
    cost = isDiag ? (cost * 34) / 24 : cost;
    graph.addEdge(nodeId, targetNode.nodeId, new ConcreteEdgeInfo(cost));
  }

  private static void createEdges(
      Size size, ConcreteGraph graph, NavType navType, IPassability passability) {
    for (int top = 0; top < size.height; ++top) {
      for (int left = 0; left < size.width; ++left) {
        CustomVec2D currentPos = new CustomVec2D(left, top);
        if (passability.cannotEnter(currentPos)) {
          continue;
        }
        Id<ConcreteNode> nodeId = getNodeByPos(graph, currentPos, size.width).nodeId;
        if (navType == NavType.Hex) {
          if (left % 2 == 0) {
            addEdge(graph, nodeId, left + 1, top - 1, size, false);
            addEdge(graph, nodeId, left - 1, top - 1, size, false);
          } else {
            addEdge(graph, nodeId, left + 1, top + 1, size, false);
            addEdge(graph, nodeId, left - 1, top + 1, size, false);
          }
          continue;
        }

        List<CustomVec2D> neighbours = NavUtils.neighbourCoordinates(currentPos, size, true);
        for (CustomVec2D neighbourPos : neighbours) {
          if (passability.cannotEnter(neighbourPos)) {
            continue;
          }
          boolean isDiagonal = CustomVec2D.diagonal(currentPos, neighbourPos);
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
        CustomVec2D position = new CustomVec2D(left, top);
        RefSupport<Integer> refVar___0 = new RefSupport<>();
        boolean isObstacle = passability.cannotEnter(position, refVar___0);
        int movementCost = refVar___0.getValue();
        ConcreteNodeInfo info = new ConcreteNodeInfo(isObstacle, movementCost, position);
        graph.addNode(nodeId, info);
      }
    }
  }
}
