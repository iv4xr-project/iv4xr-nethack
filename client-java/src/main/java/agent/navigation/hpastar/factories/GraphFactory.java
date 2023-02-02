//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package agent.navigation.hpastar.factories;

import agent.navigation.hpastar.IPassability;
import agent.navigation.hpastar.TileType;
import agent.navigation.hpastar.graph.ConcreteEdgeInfo;
import agent.navigation.hpastar.graph.ConcreteGraph;
import agent.navigation.hpastar.graph.ConcreteNode;
import agent.navigation.hpastar.graph.ConcreteNodeInfo;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.utils.RefSupport;
import eu.iv4xr.framework.spatial.IntVec2D;

public class GraphFactory {
  public static ConcreteGraph createGraph(int width, int height, IPassability passability) {
    ConcreteGraph graph = new ConcreteGraph();
    createNodes(width, height, graph, passability);
    createEdges(graph, width, height, TileType.Octile);
    return graph;
  }

  // We hardcode OCTILE for the time being
  public static ConcreteNode getNodeByPos(ConcreteGraph graph, int x, int y, int width) {
    return graph.getNode(getNodeIdFromPos(x, y, width));
  }

  public static Id<ConcreteNode> getNodeIdFromPos(int left, int top, int width) {
    return new Id<ConcreteNode>().from(top * width + left);
  }

  private static void addEdge(
      ConcreteGraph graph,
      Id<ConcreteNode> nodeId,
      int x,
      int y,
      int width,
      int height,
      boolean isDiag) {
    if (y < 0 || y >= height || x < 0 || x >= width) return;

    ConcreteNode targetNode = getNodeByPos(graph, x, y, width);
    int cost = targetNode.info.cost;
    cost = isDiag ? (cost * 34) / 24 : cost;
    graph.addEdge(nodeId, targetNode.nodeId, new ConcreteEdgeInfo(cost));
  }

  private static void createEdges(ConcreteGraph graph, int width, int height, TileType tileType) {
    for (int top = 0; top < height; ++top) {
      for (int left = 0; left < width; ++left) {
        Id<ConcreteNode> nodeId = getNodeByPos(graph, left, top, width).nodeId;
        addEdge(graph, nodeId, left, top - 1, width, height, false);
        addEdge(graph, nodeId, left, top + 1, width, height, false);
        addEdge(graph, nodeId, left - 1, top, width, height, false);
        addEdge(graph, nodeId, left + 1, top, width, height, false);
        if (tileType == TileType.Octile) {
          addEdge(graph, nodeId, left + 1, top + 1, width, height, true);
          addEdge(graph, nodeId, left - 1, top + 1, width, height, true);
          addEdge(graph, nodeId, left + 1, top - 1, width, height, true);
          addEdge(graph, nodeId, left - 1, top - 1, width, height, true);
        } else if (tileType == TileType.OctileUnicost) {
          addEdge(graph, nodeId, left + 1, top + 1, width, height, false);
          addEdge(graph, nodeId, left - 1, top + 1, width, height, false);
          addEdge(graph, nodeId, left + 1, top - 1, width, height, false);
          addEdge(graph, nodeId, left - 1, top - 1, width, height, false);
        } else if (tileType == TileType.Hex) {
          if (left % 2 == 0) {
            addEdge(graph, nodeId, left + 1, top - 1, width, height, false);
            addEdge(graph, nodeId, left - 1, top - 1, width, height, false);
          } else {
            addEdge(graph, nodeId, left + 1, top + 1, width, height, false);
            addEdge(graph, nodeId, left - 1, top + 1, width, height, false);
          }
        }
      }
    }
  }

  private static void createNodes(
      int width, int height, ConcreteGraph graph, IPassability passability) {
    for (int top = 0; top < height; ++top) {
      for (int left = 0; left < width; ++left) {
        Id<ConcreteNode> nodeId = getNodeIdFromPos(left, top, width);
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
