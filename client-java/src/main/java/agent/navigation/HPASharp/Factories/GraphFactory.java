//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package HPASharp.Factories;

import CS2JNet.JavaSupport.language.RefSupport;
import HPASharp.Graph.ConcreteEdgeInfo;
import HPASharp.Graph.ConcreteGraph;
import HPASharp.Graph.ConcreteNode;
import HPASharp.Graph.ConcreteNodeInfo;
import HPASharp.Infrastructure.Id;
import HPASharp.IPassability;
import HPASharp.Position;
import HPASharp.TileType;

public class GraphFactory
{
    public static ConcreteGraph createGraph(int width, int height, IPassability passability) throws Exception {
        ConcreteGraph graph = new ConcreteGraph();
        CreateNodes(width, height, graph, passability);
        CreateEdges(graph, width, height, TileType.Octile);
        return graph;
    }

    // We hardcode OCTILE for the time being
    public static ConcreteNode getNodeByPos(ConcreteGraph graph, int x, int y, int width) throws Exception {
        return graph.getNode(getNodeIdFromPos(x,y,width));
    }

    public static Id<ConcreteNode> getNodeIdFromPos(int left, int top, int width) throws Exception {
        return Id<ConcreteNode>.From(top * width + left);
    }

    private static void addEdge(ConcreteGraph graph, Id<ConcreteNode> nodeId, int x, int y, int width, int height, boolean isDiag) throws Exception {
        if (y < 0 || y >= height || x < 0 || x >= width)
            return ;

        ConcreteNode targetNode = getNodeByPos(graph,x,y,width);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ cost = targetNode.getInfo().getCost();
        cost = isDiag ? (cost * 34) / 24 : cost;
        graph.addEdge(nodeId,targetNode.getNodeId(),new ConcreteEdgeInfo(cost));
    }

    private static void createEdges(ConcreteGraph graph, int width, int height, TileType tileType) throws Exception {
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ top = 0;top < height;++top)
            for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ left = 0;left < width;++left)
            {
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ nodeId = GetNodeByPos(graph, left, top, width).NodeId;
                AddEdge(graph, nodeId, left, top - 1, width, height);
                AddEdge(graph, nodeId, left, top + 1, width, height);
                AddEdge(graph, nodeId, left - 1, top, width, height);
                AddEdge(graph, nodeId, left + 1, top, width, height);
                if (tileType == TileType.Octile)
                {
                    AddEdge(graph, nodeId, left + 1, top + 1, width, height, true);
                    AddEdge(graph, nodeId, left - 1, top + 1, width, height, true);
                    AddEdge(graph, nodeId, left + 1, top - 1, width, height, true);
                    AddEdge(graph, nodeId, left - 1, top - 1, width, height, true);
                }
                else if (tileType == TileType.OctileUnicost)
                {
                    AddEdge(graph, nodeId, left + 1, top + 1, width, height);
                    AddEdge(graph, nodeId, left - 1, top + 1, width, height);
                    AddEdge(graph, nodeId, left + 1, top - 1, width, height);
                    AddEdge(graph, nodeId, left - 1, top - 1, width, height);
                }
                else if (tileType == TileType.Hex)
                {
                    if (left % 2 == 0)
                    {
                        AddEdge(graph, nodeId, left + 1, top - 1, width, height);
                        AddEdge(graph, nodeId, left - 1, top - 1, width, height);
                    }
                    else
                    {
                        AddEdge(graph, nodeId, left + 1, top + 1, width, height);
                        AddEdge(graph, nodeId, left - 1, top + 1, width, height);
                    }
                }

            }
    }

    private static void createNodes(int width, int height, ConcreteGraph graph, IPassability passability) throws Exception {
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ top = 0;top < height;++top)
            for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ left = 0;left < width;++left)
            {
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ nodeId = GetNodeIdFromPos(left, top, width);
                Position position = new Position(left, top);
                int movementCost = new int();
                RefSupport<int> refVar___0 = new RefSupport<int>();
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ isObstacle = !passability.canEnter(position,refVar___0);
                movementCost = refVar___0.getValue();
                ConcreteNodeInfo info = new ConcreteNodeInfo(isObstacle, movementCost, position);
                graph.AddNode(nodeId, info);
            }
    }

}
