//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package HPASharp;

import HPASharp.ConcreteMap;
import HPASharp.Connection;
import HPASharp.Factories.GraphFactory;
import HPASharp.Graph.ConcreteGraph;
import HPASharp.Graph.ConcreteNode;
import HPASharp.Helpers;
import HPASharp.Infrastructure.Constants;
import HPASharp.Infrastructure.Id;
import HPASharp.Infrastructure.IMap;
import HPASharp.IPassability;
import HPASharp.Position;
import HPASharp.TileType;

public class ConcreteMap   implements IMap<ConcreteNode>
{
    private IPassability __Passability;
    public IPassability getPassability() {
        return __Passability;
    }

    public void setPassability(IPassability value) {
        __Passability = value;
    }

    private TileType __TileType = TileType.Hex;
    public TileType getTileType() {
        return __TileType;
    }

    public void setTileType(TileType value) {
        __TileType = value;
    }

    private int __Height = new int();
    public int getHeight() {
        return __Height;
    }

    public void setHeight(int value) {
        __Height = value;
    }

    private int __Width = new int();
    public int getWidth() {
        return __Width;
    }

    public void setWidth(int value) {
        __Width = value;
    }

    private int __MaxEdges = new int();
    public int getMaxEdges() {
        return __MaxEdges;
    }

    public void setMaxEdges(int value) {
        __MaxEdges = value;
    }

    private ConcreteGraph __Graph;
    public ConcreteGraph getGraph() {
        return __Graph;
    }

    public void setGraph(ConcreteGraph value) {
        __Graph = value;
    }

    public ConcreteMap(TileType tileType, int width, int height, IPassability passability) throws Exception {
        setPassability(passability);
        setTileType(tileType);
        setMaxEdges(Helpers.getMaxEdges(tileType));
        Height = height;
        setWidth(width);
        setGraph(GraphFactory.CreateGraph(width, height, getPassability()));
    }

    // Create a new concreteMap as a copy of another concreteMap (just copying obstacles)
    public ConcreteMap slice(int horizOrigin, int vertOrigin, int width, int height, IPassability passability) throws Exception {
        ConcreteMap slicedConcreteMap = new ConcreteMap(this.getTileType(),width,height,passability);
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ slicedMapNode : slicedConcreteMap.getGraph().getNodes())
        {
            ConcreteNode globalConcreteNode = getGraph().getNode(getNodeIdFromPos(horizOrigin + slicedMapNode.Info.Position.X,vertOrigin + slicedMapNode.Info.Position.Y));
            slicedMapNode.Info.IsObstacle = globalConcreteNode.getInfo().getIsObstacle();
            slicedMapNode.Info.Cost = globalConcreteNode.getInfo().getCost();
        }
        return slicedConcreteMap;
    }

    Width* Height = new Width*();
    public Id<ConcreteNode> getNodeIdFromPos(int x, int y) throws Exception {
        return Id<ConcreteNode>.From(y * getWidth() + x);
    }

    public int getHeuristic(Id<ConcreteNode> startNodeId, Id<ConcreteNode> targetNodeId) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ startPosition = getGraph().getNodeInfo(startNodeId).getPosition();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ targetPosition = getGraph().getNodeInfo(targetNodeId).getPosition();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ startX = startPosition.X;
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ targetX = targetPosition.X;
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ startY = startPosition.Y;
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ targetY = targetPosition.Y;
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ diffX = Math.Abs(targetX - startX);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ diffY = Math.Abs(targetY - startY);
        switch(getTileType())
        {
            case Hex:
                {
                    // Vancouver distance
                    // See P.Yap: Grid-based Path-Finding (LNAI 2338 pp.44-55)
                    /* [UNSUPPORTED] 'var' as type is unsupported "var" */ correction = 0;
                    if (diffX % 2 != 0)
                    {
                        if (targetY < startY)
                            correction = targetX % 2;
                        else if (targetY > startY)
                            correction = startX % 2;

                    }

                    // Note: formula in paper is wrong, corrected below.
                    /* [UNSUPPORTED] 'var' as type is unsupported "var" */ dist = Math.Max(0, diffY - diffX / 2 - correction) + diffX;
                    return dist * 1;
                }
            case OctileUnicost:
                return Math.Max(diffX, diffY) * Constants.COST_ONE;
            case Octile:
                int maxDiff = new int();
                int minDiff = new int();
                if (diffX > diffY)
                {
                    maxDiff = diffX;
                    minDiff = diffY;
                }
                else
                {
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

    public IEnumerable<Connection<ConcreteNode>> getConnections(Id<ConcreteNode> nodeId) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ result = new List<Connection<ConcreteNode>>();
        ConcreteNode node = getGraph().getNode(nodeId);
        ConcreteNodeInfo nodeInfo = node.getInfo();
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ edge : node.getEdges().Values)
        {
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ targetNodeId = edge.TargetNodeId;
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ targetNodeInfo = getGraph().GetNodeInfo(targetNodeId);
            if (CanJump(targetNodeInfo.Position, nodeInfo.getPosition()) && !targetNodeInfo.IsObstacle)
                result.Add(new Connection<ConcreteNode>(targetNodeId, edge.Info.Cost));

        }
        return result;
    }

    /**
    * Tells whether we can move from p1 to p2 in line. Bear in mind
    * this function does not consider intermediate points (it is
    * assumed you can jump between intermediate points)
    */
    public boolean canJump(Position p1, Position p2) throws Exception {
        if (getTileType() != getTileType().Octile && this.getTileType() != getTileType().OctileUnicost)
            return true;

        if (Helpers.areAligned(p1,p2))
            return true;

        // The following piece of code existed in the original implementation.
        // It basically checks that you do not forcefully cross a blocked diagonal.
        // Honestly, this is weird, bad designed and supposes that each position is adjacent to each other.
        ConcreteNodeInfo nodeInfo12 = getGraph().getNode(getNodeIdFromPos(p2.X,p1.Y)).getInfo();
        ConcreteNodeInfo nodeInfo21 = getGraph().getNode(getNodeIdFromPos(p1.X,p2.Y)).getInfo();
        return !(nodeInfo12.getIsObstacle() && nodeInfo21.getIsObstacle());
    }

    private List<char> getCharVector() throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ result = new List<char>();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ numberNodes = getNrNodes();
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ i = 0;i < numberNodes;++i)
            result.Add(getGraph().GetNodeInfo(Id<ConcreteNode>.From(i)).IsObstacle ? '@' : '.');
        return result;
    }

    public void printFormatted() throws Exception {
        printFormatted(getCharVector());
    }

    private void printFormatted(List<char> chars) throws Exception {
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ y = 0;y < Height;++y)
        {
            for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ x = 0;x < getWidth();++x)
            {
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ nodeId = this.GetNodeIdFromPos(x, y);
                Console.Write(chars[nodeId.IdValue]);
            }
            Console.WriteLine();
        }
    }

    public void printFormatted(List<int> path) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ chars = getCharVector();
        if (path.Count > 0)
        {
            for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ i : path)
            {
                chars[i] = 'x';
            }
            chars[path[0]] = 'T';
            chars[path[path.Count - 1]] = 'S';
        }

        PrintFormatted(chars);
    }

}
