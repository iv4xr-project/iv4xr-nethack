//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

import static java.util.stream.Collectors.groupingBy;

import agent.navigation.hpastar.graph.AbstractEdge;
import agent.navigation.hpastar.graph.AbstractEdgeInfo;
import agent.navigation.hpastar.graph.AbstractGraph;
import agent.navigation.hpastar.graph.AbstractNode;
import agent.navigation.hpastar.graph.AbstractNodeInfo;
import agent.navigation.hpastar.graph.ConcreteNode;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.infrastructure.IMap;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.search.AStar;
import agent.navigation.hpastar.search.Path;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Abstract maps represent, as the name implies, an abstraction built over the concrete map. */
public class HierarchicalMap implements IMap<AbstractNode> {
  public int height;
  public int width;
  public AbstractGraph abstractGraph = new AbstractGraph();
  public int clusterSize;
  public int maxLevel;
  public List<Cluster> clusters = new ArrayList<>();

  public int getNrNodes() {
    return abstractGraph.nodes.size();
  }

  // This list, indexed by a node id from the low level,
  // indicates to which abstract node id it maps. It is a sparse
  // array for quick access. For saving memory space, this could be implemented as a dictionary
  // NOTE: It is currently just used for insert and remove STAL
  public final Map<Id<ConcreteNode>, Id<AbstractNode>> concreteNodeIdToAbstractNodeIdMap =
      new HashMap<>();

  public AbsType type = AbsType.ABSTRACT_TILE;

  private int currentLevel;
  private int currentClusterY0;
  private int currentClusterY1;
  private int currentClusterX0;
  private int currentClusterX1;

  public void setType(TileType tileType) {
    switch (tileType) {
      case Tile:
        type = AbsType.ABSTRACT_TILE;
        break;
      case Octile:
        type = AbsType.ABSTRACT_OCTILE;
        break;
      case OctileUnicost:
        type = AbsType.ABSTRACT_OCTILE_UNICOST;
        break;
    }
  }

  public HierarchicalMap(ConcreteMap concreteMap, int clusterSize, int maxLevel) {
    this.clusterSize = clusterSize;
    this.maxLevel = maxLevel;
    setType(concreteMap.tileType);
    height = concreteMap.height;
    width = concreteMap.width;
  }

  public int getHeuristic(Id<AbstractNode> startNodeId, Id<AbstractNode> targetNodeId) {
    IntVec2D startPos = abstractGraph.getNodeInfo(startNodeId).position;
    IntVec2D targetPos = abstractGraph.getNodeInfo(targetNodeId).position;
    int diffY = Math.abs(startPos.y - targetPos.y);
    int diffX = Math.abs(startPos.x - targetPos.x);
    return (diffY + diffX) * Constants.COST_ONE;
  }

  // Manhattan distance, after testing a bit for hierarchical searches we do not need
  // the level of precision of Diagonal distance or euclidean distance
  public Cluster findClusterForPosition(IntVec2D pos) {
    Cluster foundCluster = null;
    for (Cluster cluster : clusters) {
      if (cluster.origin.y <= pos.y
          && pos.y < cluster.origin.y + cluster.size.height
          && cluster.origin.x <= pos.x
          && pos.x < cluster.origin.x + cluster.size.width) {
        foundCluster = cluster;
        break;
      }
    }
    return foundCluster;
  }

  public void addEdge(
      Id<AbstractNode> sourceNodeId,
      Id<AbstractNode> destNodeId,
      int cost,
      int level,
      boolean inter,
      List<Id<AbstractNode>> pathPathNodes) {
    AbstractEdgeInfo edgeInfo = new AbstractEdgeInfo(cost, level, inter);
    edgeInfo.innerLowerLevelPath = pathPathNodes;
    abstractGraph.addEdge(sourceNodeId, destNodeId, edgeInfo);
  }

  public void addEdge(Id<AbstractNode> sourceNodeId, Id<AbstractNode> destNodeId, int cost) {
    addEdge(sourceNodeId, destNodeId, cost, 1, false, null);
  }

  public List<AbstractEdge> getNodeEdges(Id<ConcreteNode> nodeId) {
    AbstractNode node = abstractGraph.getNode(concreteNodeIdToAbstractNodeIdMap.get(nodeId));
    return new ArrayList<>(node.edges.values());
  }

  public Cluster getCluster(Id<Cluster> id) {
    return clusters.get(id.getIdValue());
  }

  /** Gets the neighbours(successors) of the nodeId for the level set in the currentLevel */
  public Iterable<Connection<AbstractNode>> getConnections(Id<AbstractNode> nodeId) {
    AbstractNode node = abstractGraph.getNode(nodeId);
    Map<Id<AbstractNode>, AbstractEdge> edges = node.edges;
    List<Connection<AbstractNode>> result = new ArrayList<>();
    for (AbstractEdge edge : edges.values()) {
      AbstractEdgeInfo edgeInfo = edge.info;
      if (!isValidEdgeForLevel(edgeInfo, currentLevel)) continue;

      Id<AbstractNode> targetNodeId = edge.targetNodeId;
      AbstractNodeInfo targetNodeInfo = abstractGraph.getNodeInfo(targetNodeId);
      if (!positionInCurrentCluster(targetNodeInfo.position)) {
        continue;
      }

      result.add(new Connection<>(targetNodeId, edgeInfo.cost));
    }
    return result;
  }

  public void removeAbstractNode(Id<AbstractNode> abstractNodeId) {
    AbstractNodeInfo abstractNodeInfo = abstractGraph.getNodeInfo(abstractNodeId);
    Cluster cluster = clusters.get(abstractNodeInfo.clusterId.getIdValue());
    cluster.removeLastEntranceRecord();
    concreteNodeIdToAbstractNodeIdMap.remove(abstractNodeInfo.concreteNodeId);
    abstractGraph.removeEdgesFromAndToNode(abstractNodeId);
    abstractGraph.removeLastNode();
  }

  private static boolean isValidEdgeForLevel(AbstractEdgeInfo edgeInfo, int level) {
    if (edgeInfo.isInterClusterEdge) {
      return edgeInfo.level >= level;
    }

    return edgeInfo.level == level;
  }

  public boolean positionInCurrentCluster(IntVec2D position) {
    int y = position.y;
    int x = position.x;
    return y >= currentClusterY0
        && y <= currentClusterY1
        && x >= currentClusterX0
        && x <= currentClusterX1;
  }

  // Define the offset between two clusters in this level (each level doubles the cluster size)
  private int getOffset(int level) {
    return clusterSize * (1 << (level - 1));
  }

  public void setAllMapAsCurrentCluster() {
    currentClusterY0 = 0;
    currentClusterY1 = height - 1;
    currentClusterX0 = 0;
    currentClusterX1 = width - 1;
  }

  public void setCurrentClusterByPositionAndLevel(IntVec2D pos, int level) {
    int offset = getOffset(level);
    int nodeY = pos.y;
    int nodeX = pos.x;
    currentClusterY0 = nodeY - (nodeY % offset);
    currentClusterY1 = Math.min(this.height - 1, this.currentClusterY0 + offset - 1);
    currentClusterX0 = nodeX - (nodeX % offset);
    currentClusterX1 = Math.min(this.width - 1, this.currentClusterX0 + offset - 1);
  }

  public boolean belongToSameCluster(
      Id<AbstractNode> node1Id, Id<AbstractNode> node2Id, int level) {
    IntVec2D node1Pos = abstractGraph.getNodeInfo(node1Id).position;
    IntVec2D node2Pos = abstractGraph.getNodeInfo(node2Id).position;
    int offset = getOffset(level);
    int currentRow1 = node1Pos.y - (node1Pos.y % offset);
    int currentRow2 = node2Pos.y - (node2Pos.y % offset);
    int currentCol1 = node1Pos.x - (node1Pos.x % offset);
    int currentCol2 = node2Pos.x - (node2Pos.x % offset);
    if (currentRow1 != currentRow2) {
      return false;
    }

    if (currentCol1 != currentCol2) {
      return false;
    }

    return true;
  }

  public void setCurrentLevelForSearches(int level) {
    currentLevel = level;
  }

  private boolean isValidAbstractNodeForLevel(Id<AbstractNode> abstractNodeId, int level) {
    return abstractGraph.getNodeInfo(abstractNodeId).level >= level;
  }

  private int getEntrancePointLevel(EntrancePoint entrancePoint) {
    return abstractGraph.getNodeInfo(entrancePoint.abstractNodeId).level;
  }

  public void createHierarchicalEdges() {
    for (int level = 2; level <= maxLevel; level++) {
      // Starting from level 2 denotes a serious mess on design, because lvl 1 is
      // used by the clusters.
      setCurrentLevelForSearches(level - 1);
      int n = 1 << (level - 1);
      // Group clusters by their level. Each subsequent level doubles the amount of clusters in each
      // group
      //            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ clusterGroups =
      // clusters.GroupBy(/* [UNSUPPORTED] to translate lambda expressions we need an explicit
      // delegate type, try adding a cast "(cl) => {
      //                return "{cl.ClusterX / n}_{cl.ClusterY / n}";
      //            }" */);

      Map<String, List<Cluster>> clusterGroups =
          clusters.stream()
              .collect(
                  groupingBy(
                      (Cluster cl) -> String.format("%d_%d", cl.clusterX / n, cl.clusterY / n)));
      for (Map.Entry<String, List<Cluster>> clusterGroup : clusterGroups.entrySet()) {
        final int tempLvl = level;
        Stream<EntrancePoint> entrancesInClusterGroupStream =
            clusterGroup.getValue().stream().flatMap((Cluster cl) -> cl.entrancePoints.stream());
        List<EntrancePoint> entrancesInClusterGroup =
            entrancesInClusterGroupStream
                .filter((EntrancePoint entrance) -> this.getEntrancePointLevel(entrance) >= tempLvl)
                .collect(Collectors.toList());

        Optional<EntrancePoint> firstEntrance = entrancesInClusterGroup.stream().findFirst();
        if (firstEntrance.isEmpty()) {
          continue;
        }

        IntVec2D entrancePosition =
            abstractGraph.getNode(firstEntrance.get().abstractNodeId).info.position;
        setCurrentClusterByPositionAndLevel(entrancePosition, level);
        for (EntrancePoint entrance1 : entrancesInClusterGroup) {
          for (EntrancePoint entrance2 : entrancesInClusterGroup) {
            if (entrance1 == entrance2
                || !isValidAbstractNodeForLevel(entrance1.abstractNodeId, level)
                || !isValidAbstractNodeForLevel(entrance2.abstractNodeId, level)) {
              continue;
            }

            addEdgesBetweenAbstractNodes(entrance1.abstractNodeId, entrance2.abstractNodeId, level);
          }
        }
      }
    }
  }

  public void addEdgesBetweenAbstractNodes(
      Id<AbstractNode> srcAbstractNodeId, Id<AbstractNode> destAbstractNodeId, int level) {
    AStar<AbstractNode> search = new AStar<>(this, srcAbstractNodeId, destAbstractNodeId);
    Path<AbstractNode> path = search.findPath();
    if (path.pathCost >= 0) {
      addEdge(
          srcAbstractNodeId,
          destAbstractNodeId,
          path.pathCost,
          level,
          false,
          new ArrayList<>(path.pathNodes));
      Collections.reverse(path.pathNodes);
      addEdge(destAbstractNodeId, srcAbstractNodeId, path.pathCost, level, false, path.pathNodes);
    }
  }

  public void addEdgesToOtherEntrancesInCluster(AbstractNodeInfo abstractNodeInfo, int level) {
    setCurrentLevelForSearches(level - 1);
    setCurrentClusterByPositionAndLevel(abstractNodeInfo.position, level);
    for (Cluster cluster : clusters) {
      if (cluster.origin.x >= currentClusterX0
          && cluster.origin.x <= currentClusterX1
          && cluster.origin.y >= currentClusterY0
          && cluster.origin.y <= currentClusterY1) {
        for (EntrancePoint entrance : cluster.entrancePoints) {
          if (abstractNodeInfo.id == entrance.abstractNodeId
              || !isValidAbstractNodeForLevel(entrance.abstractNodeId, level)) {
            continue;
          }

          addEdgesBetweenAbstractNodes(abstractNodeInfo.id, entrance.abstractNodeId, level);
        }
      }
    }
  }

  public void addHierarchicalEdgesForAbstractNode(Id<AbstractNode> abstractNodeId) {
    AbstractNodeInfo abstractNodeInfo = abstractGraph.getNodeInfo(abstractNodeId);
    int oldLevel = abstractNodeInfo.level;
    abstractNodeInfo.level = maxLevel;
    for (int level = oldLevel + 1; level <= maxLevel; level++) {
      addEdgesToOtherEntrancesInCluster(abstractNodeInfo, level);
    }
  }
}