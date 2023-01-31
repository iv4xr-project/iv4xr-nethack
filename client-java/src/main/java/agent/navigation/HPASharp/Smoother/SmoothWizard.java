//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:35
//

package HPASharp.Smoother;

import HPASharp.ConcreteMap;
import HPASharp.ConcretePathNode;
import HPASharp.Graph.ConcreteNode;
import HPASharp.Infrastructure.Constants;
import HPASharp.Infrastructure.Id;
import HPASharp.IPathNode;
import HPASharp.Position;
import HPASharp.Smoother.Direction;
import HPASharp.TileType;

public class SmoothWizard
{
    private List<IPathNode> __InitialPath = new List<IPathNode>();
    public List<IPathNode> getInitialPath() {
        return __InitialPath;
    }

    public void setInitialPath(List<IPathNode> value) {
        __InitialPath = value;
    }

    private static final Id<ConcreteNode> INVALID_ID = Id<ConcreteNode>.From(Constants.NO_NODE);
    private final ConcreteMap _concreteMap;
    // This is a dictionary, indexed by nodeId, that tells in which order does this node occupy in the path
    private final Dictionary<int, int> _pathMap = new Dictionary<int, int>();
    public SmoothWizard(ConcreteMap concreteMap, List<IPathNode> path) throws Exception {
        setInitialPath(path);
        _concreteMap = concreteMap;
        _pathMap = new Dictionary<int, int>();
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ i = 0;i < getInitialPath().Count;i++)
        {
            _pathMap[getInitialPath()[i].IdValue] = i + 1;
        }
    }

    private Position getPosition(Id<ConcreteNode> nodeId) throws Exception {
        return _concreteMap.Graph.GetNodeInfo(nodeId).Position;
    }

    public List<IPathNode> smoothPath() throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ smoothedPath = new List<IPathNode>();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ smoothedConcretePath = new List<ConcretePathNode>();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ index = 0;
        for (;index < getInitialPath().Count && getInitialPath()[index] instanceof ConcretePathNode;index++)
        {
            ConcretePathNode pathNode = (ConcretePathNode)getInitialPath()[index];
            if (smoothedConcretePath.Count == 0)
                smoothedConcretePath.Add(pathNode);

            // add this node to the smoothed path
            if (smoothedConcretePath[smoothedConcretePath.Count - 1].Id != pathNode.Id)
            {
                // It's possible that, when smoothing, the next node that will be put in the path
                // will not be adjacent. In those cases, since OpenRA requires a continuous path
                // without breakings, we should calculate a new path for that section
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ lastNodeInSmoothedPath = smoothedConcretePath[smoothedConcretePath.Count - 1];
                ConcretePathNode currentNodeInPath = pathNode;
                if (!AreAdjacent(GetPosition(lastNodeInSmoothedPath.Id), getPosition(currentNodeInPath.Id)))
                {
                    /* [UNSUPPORTED] 'var' as type is unsupported "var" */ intermediatePath = GenerateIntermediateNodes(smoothedConcretePath[smoothedConcretePath.Count - 1].Id, pathNode.Id);
                    for (int i = 1;i < intermediatePath.Count;i++)
                    {
                        smoothedConcretePath.Add(new ConcretePathNode(intermediatePath[i]));
                    }
                }

                smoothedConcretePath.Add(pathNode);
            }

            index = DecideNextNodeToConsider(index);
        }
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ pathNode : smoothedConcretePath)
        {
            smoothedPath.Add(pathNode);
        }
        for (;index < getInitialPath().Count;index++)
        {
            smoothedPath.Add(getInitialPath()[index]);
        }
        return smoothedPath;
    }

    private int decideNextNodeToConsider(int index) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ newIndex = index;
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ dir = ((Enum)Direction.North).ordinal();dir <= ((Enum)Direction.NorthWest).ordinal();dir++)
        {
            if (_concreteMap.TileType == TileType.Tile && dir > ((Enum)Direction.West).ordinal())
                break;

            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ seenPathNode = AdvanceThroughDirection(Id<ConcreteNode>.From(getInitialPath()[index].IdValue), dir);
            if (seenPathNode == INVALID_ID)
                continue;

            // No node in advance in that direction, just continue
            if (index > 0 && seenPathNode.IdValue == getInitialPath()[index - 1].IdValue)
                continue;

            // If the point we are advancing is the same as the previous one, we didn't
            // improve at all. Just continue looking other directions
            if (index < getInitialPath().Count - 1 && seenPathNode.IdValue == getInitialPath()[index + 1].IdValue)
                continue;

            // If the point we are advancing is the same as a next node in the path,
            // we didn't improve either. Continue next direction
            newIndex = _pathMap[seenPathNode.IdValue] - 2;
            break;
        }
        return newIndex;
    }

    // count the path reduction (e.g., 2)
    private static boolean areAdjacent(Position a, Position b) throws Exception {
        return Math.Abs(a.X - b.X) + Math.Abs(a.Y - b.Y) <= 2;
    }

    // if the Manhattan distance between a and b is > 2, then they are not
    // (At least on OCTILE)
    private List<Id<ConcreteNode>> generateIntermediateNodes(Id<ConcreteNode> nodeid1, Id<ConcreteNode> nodeid2) throws Exception {
        AStar<ConcreteNode> search = new AStar<ConcreteNode>(_concreteMap, nodeid1, nodeid2);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ path = search.findPath();
        return path.PathNodes;
    }

    /**
    * Returns the next node in the init path in a straight line that
    * lies in the same direction as the origin node
    */
    private Id<ConcreteNode> advanceThroughDirection(Id<ConcreteNode> originId, int direction) throws Exception {
        Id<ConcreteNode> nodeId = originId;
        Id<ConcreteNode> lastNodeId = originId;
        while (true)
        {
            // advance in the given direction
            nodeId = advanceNode(nodeId,direction);
            // If in the direction we advanced there was an invalid node or we cannot enter the node,
            // just return that no node was found
            if (nodeId == INVALID_ID || !_concreteMap.CanJump(getPosition(nodeId), getPosition(lastNodeId)))
                return INVALID_ID;

            // Otherwise, if the node we advanced was contained in the original path, and
            // it was positioned after the node we are analyzing, return it
            if (_pathMap.ContainsKey(nodeId.getIdValue()) && _pathMap[nodeId.getIdValue()] > _pathMap[originId.getIdValue()])
            {
                return nodeId;
            }

            // If we have found an obstacle, just return that no next node to advance was found
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ newNodeInfo = _concreteMap.Graph.GetNodeInfo(nodeId);
            if (newNodeInfo.IsObstacle)
                return INVALID_ID;

            lastNodeId = nodeId;
        }
    }

    private Id<ConcreteNode> advanceNode(Id<ConcreteNode> nodeId, int direction) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ nodeInfo = _concreteMap.Graph.GetNodeInfo(nodeId);
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ y = nodeInfo.Position.Y;
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ x = nodeInfo.Position.X;
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ tilingGraph = _concreteMap.Graph;
        Func<int, int, ConcreteNode> getNode = /* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(top, left) => {
            return tilingGraph.GetNode(_concreteMap.GetNodeIdFromPos(top, left));
        }" */;
        switch(Direction.values()[direction])
        {
            case North:
                if (y == 0)
                    return INVALID_ID;

                return getNode(x, y - 1).NodeId;
            case East:
                if (x == _concreteMap.Width - 1)
                    return INVALID_ID;

                return getNode(x + 1, y).NodeId;
            case South:
                if (y == _concreteMap.Height - 1)
                    return INVALID_ID;

                return getNode(x, y + 1).NodeId;
            case West:
                if (x == 0)
                    return INVALID_ID;

                return getNode(x - 1, y).NodeId;
            case NorthEast:
                if (y == 0 || x == _concreteMap.Width - 1)
                    return INVALID_ID;

                return getNode(x + 1, y - 1).NodeId;
            case SouthEast:
                if (y == _concreteMap.Height - 1 || x == _concreteMap.Width - 1)
                    return INVALID_ID;

                return getNode(x + 1, y + 1).NodeId;
            case SouthWest:
                if (y == _concreteMap.Height - 1 || x == 0)
                    return INVALID_ID;

                return getNode(x - 1, y + 1).NodeId;
            case NorthWest:
                if (y == 0 || x == 0)
                    return INVALID_ID;

                return getNode(x - 1, y - 1).NodeId;
            default:
                return INVALID_ID;

        }
    }

}
