//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:35
//

package HPASharp.Search;

import HPASharp.AbstractPathNode;
import HPASharp.Cluster;
import HPASharp.ConcretePathNode;
import HPASharp.Graph.AbstractNode;
import HPASharp.Graph.ConcreteNode;
import HPASharp.HierarchicalMap;
import HPASharp.Infrastructure.Id;
import HPASharp.IPathNode;
import HPASharp.Search.Path;

public class HierarchicalSearch
{
    public List<AbstractPathNode> doHierarchicalSearch(HierarchicalMap map, Id<AbstractNode> startNodeId, Id<AbstractNode> targetNodeId, int maxSearchLevel, int maxPathsToRefine) throws Exception {
        List<AbstractPathNode> path = GetPath(map, startNodeId, targetNodeId, maxSearchLevel, true);
        if (path.Count == 0)
            return path;

        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ level = maxSearchLevel;level > 1;level--)
            path = RefineAbstractPath(map, path, level, maxPathsToRefine);
        return path;
    }

    private List<AbstractPathNode> getPath(HierarchicalMap map, Id<AbstractNode> startNodeId, Id<AbstractNode> targetNodeId, int level, boolean mainSearch) throws Exception {
        map.setCurrentLevelForSearches(level);
        AbstractNodeInfo nodeInfo = map.getAbstractGraph().getNodeInfo(startNodeId);
        // TODO: This could be perfectly replaced by cached paths in the clusters!
        Path<AbstractNode> path;
        if (!mainSearch)
        {
            map.SetCurrentClusterByPositionAndLevel(nodeInfo.getPosition(), level + 1);
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ edgeInfo = map.getAbstractGraph().getEdges(startNodeId)[targetNodeId].Info;
            path = new Path<AbstractNode>(edgeInfo.InnerLowerLevelPath, edgeInfo.Cost);
        }
        else
        {
            map.setAllMapAsCurrentCluster();
            AStar<AbstractNode> search = new AStar<AbstractNode>(map,startNodeId,targetNodeId);
            path = search.findPath();
        }
        if (path.getPathCost() == -1)
        {
            return new List<AbstractPathNode>();
        }

        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ result = new List<AbstractPathNode>(path.getPathNodes().Count);
        for (Object __dummyForeachVar0 : path.getPathNodes())
        {
            Id<AbstractNode> abstractNodeId = (Id<AbstractNode>)__dummyForeachVar0;
            result.Add(new AbstractPathNode(abstractNodeId,level));
        }
        return result;
    }

    public List<AbstractPathNode> refineAbstractPath(HierarchicalMap map, List<AbstractPathNode> path, int level, int maxPathsToRefine) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ refinedAbstractPath = new List<AbstractPathNode>();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ calculatedPaths = 0;
        if (path.Count == 0)
            return refinedAbstractPath;

        refinedAbstractPath.Add(new AbstractPathNode(path[0].Id, level - 1));
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ i = 1;i < path.Count;i++)
        {
            if (path[i].Level == level && path[i].Level == path[i - 1].Level && map.BelongToSameCluster(path[i].Id, path[i - 1].Id, level) && calculatedPaths < maxPathsToRefine)
            {
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ interNodePath = GetPath(map, path[i - 1].Id, path[i].Id, level - 1, false);
                for (int j = 1;j < interNodePath.Count;j++)
                {
                    refinedAbstractPath.Add(interNodePath[j]);
                }
                calculatedPaths++;
            }
            else
                refinedAbstractPath.Add(new AbstractPathNode(path[i].Id, level - 1));
        }
        return refinedAbstractPath;
    }

    public List<IPathNode> abstractPathToLowLevelPath(HierarchicalMap map, List<AbstractPathNode> abstractPath, int mapWidth, int maxPathsToCalculate) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ result = new List<IPathNode>();
        if (abstractPath.Count == 0)
            return result;

        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ calculatedPaths = 0;
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ lastAbstractNodeId = abstractPath[0].Id;
        if (abstractPath[0].Level != 1)
        {
            result.Add(abstractPath[0]);
        }
        else
        {
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ abstractNode = map.getAbstractGraph().GetNodeInfo(lastAbstractNodeId);
            result.Add(new ConcretePathNode(abstractNode.ConcreteNodeId));
        }
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ currentPoint = 1;currentPoint < abstractPath.Count;currentPoint++)
        {
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ currentAbstractNodeId = abstractPath[currentPoint].Id;
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ lastNodeInfo = map.getAbstractGraph().GetNodeInfo(lastAbstractNodeId);
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ currentNodeInfo = map.getAbstractGraph().GetNodeInfo(currentAbstractNodeId);
            if (lastAbstractNodeId == currentAbstractNodeId)
            {
                continue;
            }

            // We cannot compute a low level path from a level which is higher than lvl 1
            // (obvious...) therefore, ignore any non-refined path
            if (abstractPath[currentPoint].Level != 1)
            {
                result.Add(abstractPath[currentPoint]);
                continue;
            }

            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ currentNodeClusterId = currentNodeInfo.ClusterId;
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ lastNodeClusterId = lastNodeInfo.ClusterId;
            if (currentNodeClusterId == lastNodeClusterId && calculatedPaths < maxPathsToCalculate)
            {
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ cluster = map.GetCluster(currentNodeClusterId);
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ localPath = cluster.GetPath(lastAbstractNodeId, currentAbstractNodeId);
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ concretePath = new List<IPathNode>();
                for (int i = 1;i < localPath.Count;i++)
                {
                    int concreteNodeId = LocalNodeId2ConcreteNodeId(localPath[i].IdValue, cluster, mapWidth);
                    concretePath.Add(new ConcretePathNode(Id<ConcreteNode>.From(concreteNodeId)));
                }
                result.AddRange(concretePath);
                calculatedPaths++;
            }
            else
            {
                // Inter-cluster edge
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ lastConcreteNodeId = lastNodeInfo.ConcreteNodeId;
                /* [UNSUPPORTED] 'var' as type is unsupported "var" */ currentConcreteNodeId = currentNodeInfo.ConcreteNodeId;
                if (((ConcretePathNode)result[result.Count - 1]).Id != lastConcreteNodeId)
                    result.Add(new ConcretePathNode(lastConcreteNodeId));

                if (((ConcretePathNode)result[result.Count - 1]).Id != currentConcreteNodeId)
                    result.Add(new ConcretePathNode(currentConcreteNodeId));

            }
            lastAbstractNodeId = currentAbstractNodeId;
        }
        return result;
    }

    private static int localNodeId2ConcreteNodeId(int localId, Cluster cluster, int width) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ localX = localId % cluster.getSize().getWidth();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ localY = localId / cluster.getSize().getWidth();
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ result = (localY + cluster.getOrigin().Y) * width + (localX + cluster.getOrigin().X);
        return result;
    }

    private static int globalId2LocalId(int globalId, Cluster cluster, int width) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ globalY = globalId / width;
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ globalX = globalId % width;
        return (globalY - cluster.getOrigin().Y) * cluster.getSize().getWidth() + (globalX - cluster.getOrigin().X);
    }

}
