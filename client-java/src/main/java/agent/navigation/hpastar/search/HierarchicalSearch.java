//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:35
//

package agent.navigation.hpastar.search;

import agent.navigation.hpastar.AbstractPathNode;
import agent.navigation.hpastar.Cluster;
import agent.navigation.hpastar.ConcretePathNode;
import agent.navigation.hpastar.HierarchicalMap;
import agent.navigation.hpastar.IPathNode;
import agent.navigation.hpastar.graph.AbstractEdgeInfo;
import agent.navigation.hpastar.graph.AbstractNode;
import agent.navigation.hpastar.graph.AbstractNodeInfo;
import agent.navigation.hpastar.graph.ConcreteNode;
import agent.navigation.hpastar.infrastructure.Id;
import java.util.ArrayList;
import java.util.List;
import util.Loggers;

public class HierarchicalSearch {
  public List<AbstractPathNode> doHierarchicalSearch(
      HierarchicalMap map,
      Id<AbstractNode> startNodeId,
      Id<AbstractNode> targetNodeId,
      int maxSearchLevel,
      int maxPathsToRefine) {
    List<AbstractPathNode> path = getPath(map, startNodeId, targetNodeId, maxSearchLevel, true);
    if (path.isEmpty()) {
      return path;
    }

    for (int level = maxSearchLevel; level > 1; level--) {
      path = refineAbstractPath(map, path, level, maxPathsToRefine);
    }
    return path;
  }

  private List<AbstractPathNode> getPath(
      HierarchicalMap map,
      Id<AbstractNode> startNodeId,
      Id<AbstractNode> targetNodeId,
      int level,
      boolean mainSearch) {
    map.setCurrentLevelForSearches(level);
    // TODO: This could be perfectly replaced by cached paths in the clusters!
    Path<AbstractNode> path;
    if (!mainSearch) {
      AbstractNodeInfo nodeInfo = map.abstractGraph.getNodeInfo(startNodeId);
      map.setCurrentClusterByPositionAndLevel(nodeInfo.position, level + 1);
      AbstractEdgeInfo edgeInfo = map.abstractGraph.getEdges(startNodeId).get(targetNodeId).info;
      path = new Path<AbstractNode>(edgeInfo.innerLowerLevelPath, edgeInfo.cost);
    } else {
      map.setAllMapAsCurrentCluster();
      AStar<AbstractNode> search = new AStar<>(map, startNodeId, targetNodeId);
      Loggers.HPALogger.info(
          "Search: %s -> %s, %s %s",
          startNodeId,
          targetNodeId,
          map.abstractGraph.getNodeInfo(startNodeId),
          map.abstractGraph.getNodeInfo(targetNodeId));
      path = search.findPath();
    }
    if (path.pathCost == -1) {
      return new ArrayList<>();
    }

    List<AbstractPathNode> result = new ArrayList<>(path.pathNodes.size());
    for (Id<AbstractNode> abstractNodeId : path.pathNodes) {
      result.add(new AbstractPathNode(abstractNodeId, level));
    }
    return result;
  }

  public List<AbstractPathNode> refineAbstractPath(
      HierarchicalMap map, List<AbstractPathNode> path, int level, int maxPathsToRefine) {
    List<AbstractPathNode> refinedAbstractPath = new ArrayList<>();
    int calculatedPaths = 0;
    if (path.isEmpty()) {
      return refinedAbstractPath;
    }

    refinedAbstractPath.add(new AbstractPathNode(path.get(0).id, level - 1));
    for (int i = 1; i < path.size(); i++) {
      if (path.get(i).level == level
          && path.get(i).level == path.get(i - 1).level
          && map.belongToSameCluster(path.get(i).id, path.get(i - 1).id, level)
          && calculatedPaths < maxPathsToRefine) {
        List<AbstractPathNode> interNodePath =
            getPath(map, path.get(i - 1).id, path.get(i).id, level - 1, false);
        for (int j = 1; j < interNodePath.size(); j++) {
          refinedAbstractPath.add(interNodePath.get(j));
        }
        calculatedPaths++;
      } else {
        refinedAbstractPath.add(new AbstractPathNode(path.get(i).id, level - 1));
      }
    }
    return refinedAbstractPath;
  }

  public List<IPathNode> abstractPathToLowLevelPath(
      HierarchicalMap map,
      List<AbstractPathNode> abstractPath,
      int mapWidth,
      int maxPathsToCalculate) {
    List<IPathNode> result = new ArrayList<>();
    if (abstractPath.isEmpty()) {
      return result;
    }

    int calculatedPaths = 0;
    Id<AbstractNode> lastAbstractNodeId = abstractPath.get(0).id;
    if (abstractPath.get(0).level != 1) {
      result.add(abstractPath.get(0));
    } else {
      AbstractNodeInfo abstractNode = map.abstractGraph.getNodeInfo(lastAbstractNodeId);
      result.add(new ConcretePathNode(abstractNode.concreteNodeId));
    }
    for (int currentPoint = 1; currentPoint < abstractPath.size(); currentPoint++) {
      Id<AbstractNode> currentAbstractNodeId = abstractPath.get(currentPoint).id;
      AbstractNodeInfo lastNodeInfo = map.abstractGraph.getNodeInfo(lastAbstractNodeId);
      AbstractNodeInfo currentNodeInfo = map.abstractGraph.getNodeInfo(currentAbstractNodeId);
      if (lastAbstractNodeId == currentAbstractNodeId) {
        continue;
      }

      // We cannot compute a low level path from a level which is higher than lvl 1
      // (obvious...) therefore, ignore any non-refined path
      if (abstractPath.get(currentPoint).level != 1) {
        result.add(abstractPath.get(currentPoint));
        continue;
      }

      Id<Cluster> currentNodeClusterId = currentNodeInfo.clusterId;
      Id<Cluster> lastNodeClusterId = lastNodeInfo.clusterId;
      if (currentNodeClusterId == lastNodeClusterId && calculatedPaths < maxPathsToCalculate) {
        Cluster cluster = map.getCluster(currentNodeClusterId);
        List<Id<ConcreteNode>> localPath =
            cluster.getPath(lastAbstractNodeId, currentAbstractNodeId);

        // When local path is null, it means there is no direct path within the cluster
        // Then we need to treat it as an intercluster path
        if (localPath != null) {
          for (int i = 0; i < localPath.size(); i++) {
            // If there is only one node, we do need to add it isntead of skipping it
            if (i == 0 && localPath.size() != 1) {
              continue;
            }
            int concreteNodeId =
                localNodeId2ConcreteNodeId(localPath.get(i).getIdValue(), cluster, mapWidth);
            result.add(new ConcretePathNode(new Id<ConcreteNode>().from(concreteNodeId)));
          }
          calculatedPaths++;
          lastAbstractNodeId = currentAbstractNodeId;
          continue;
        }
      }
      // Inter-cluster edge
      Id<ConcreteNode> lastConcreteNodeId = lastNodeInfo.concreteNodeId;
      Id<ConcreteNode> currentConcreteNodeId = currentNodeInfo.concreteNodeId;
      if (!((ConcretePathNode) result.get(result.size() - 1)).id.equals(lastConcreteNodeId)) {
        result.add(new ConcretePathNode(lastConcreteNodeId));
      }

      if (!((ConcretePathNode) result.get(result.size() - 1)).id.equals(currentConcreteNodeId)) {
        result.add(new ConcretePathNode(currentConcreteNodeId));
      }
      lastAbstractNodeId = currentAbstractNodeId;
    }
    return result;
  }

  private static int localNodeId2ConcreteNodeId(int localId, Cluster cluster, int width) {
    int localX = localId % cluster.size.width;
    int localY = localId / cluster.size.width;
    return (localY + cluster.origin.y) * width + (localX + cluster.origin.x);
  }

  private static int globalId2LocalId(int globalId, Cluster cluster, int width) {
    int globalY = globalId / width;
    int globalX = globalId % width;
    return (globalY - cluster.origin.y) * cluster.size.width + (globalX - cluster.origin.x);
  }
}
