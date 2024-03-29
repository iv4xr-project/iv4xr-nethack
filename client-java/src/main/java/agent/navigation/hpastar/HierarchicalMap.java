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
import agent.navigation.hpastar.search.IdPath;
import java.util.*;
import java.util.stream.Stream;
import nethack.enums.Color;
import util.ColoredStringBuilder;
import util.CustomVec2D;

/** Abstract maps represent, as the name implies, an abstraction built over the concrete map. */
public class HierarchicalMap implements IMap<AbstractNode> {
  public final Size size;
  public final AbstractGraph abstractGraph = new AbstractGraph();
  public final int clusterSize;
  public final int maxLevel;
  public final List<Cluster> clusters = new ArrayList<>();

  public int getNrNodes() {
    return abstractGraph.nextId;
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

  public void setType(NavType navType) {
    switch (navType) {
      case Tile -> type = AbsType.ABSTRACT_TILE;
      case Octile -> type = AbsType.ABSTRACT_OCTILE;
      case OctileUnicost -> type = AbsType.ABSTRACT_OCTILE_UNICOST;
    }
  }

  public HierarchicalMap(NavType navType, int clusterSize, int maxLevel, Size size) {
    this.clusterSize = clusterSize;
    this.maxLevel = maxLevel;
    setType(navType);
    this.size = size;
  }

  public int getHeuristic(Id<AbstractNode> startNodeId, Id<AbstractNode> targetNodeId) {
    CustomVec2D startPos = abstractGraph.getNodeInfo(startNodeId).position;
    CustomVec2D targetPos = abstractGraph.getNodeInfo(targetNodeId).position;
    int diffY = Math.abs(startPos.y - targetPos.y);
    int diffX = Math.abs(startPos.x - targetPos.x);
    return (diffY + diffX) * Constants.COST_ONE;
  }

  // Manhattan distance, after testing a bit for hierarchical searches we do not need
  // the level of precision of Diagonal distance or euclidean distance
  public Cluster findClusterForPosition(CustomVec2D pos) {
    size.withinBounds(pos);
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
    //    System.out.printf("AbsGraph AddEdge %s -> %s%n", sourceNodeId, destNodeId);
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
    cluster.removeRelevantEntranceRecord(abstractNodeId);
    concreteNodeIdToAbstractNodeIdMap.remove(abstractNodeInfo.concreteNodeId);
    abstractGraph.removeEdgesFromAndToNode(abstractNodeId);
    abstractGraph.removeNode(abstractNodeId);
  }

  private static boolean isValidEdgeForLevel(AbstractEdgeInfo edgeInfo, int level) {
    if (edgeInfo.isInterClusterEdge) {
      return edgeInfo.level >= level;
    }

    return edgeInfo.level == level;
  }

  public boolean positionInCurrentCluster(CustomVec2D position) {
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
    currentClusterY1 = size.height - 1;
    currentClusterX0 = 0;
    currentClusterX1 = size.width - 1;
  }

  public void setCurrentClusterByPositionAndLevel(CustomVec2D pos, int level) {
    int offset = getOffset(level);
    int nodeY = pos.y;
    int nodeX = pos.x;
    currentClusterY0 = nodeY - (nodeY % offset);
    currentClusterY1 = Math.min(size.height - 1, currentClusterY0 + offset - 1);
    currentClusterX0 = nodeX - (nodeX % offset);
    currentClusterX1 = Math.min(size.width - 1, currentClusterX0 + offset - 1);
  }

  public boolean belongToSameCluster(
      Id<AbstractNode> node1Id, Id<AbstractNode> node2Id, int level) {
    CustomVec2D node1Pos = abstractGraph.getNodeInfo(node1Id).position;
    CustomVec2D node2Pos = abstractGraph.getNodeInfo(node2Id).position;
    int offset = getOffset(level);
    int currentRow1 = node1Pos.y - (node1Pos.y % offset);
    int currentRow2 = node2Pos.y - (node2Pos.y % offset);
    int currentCol1 = node1Pos.x - (node1Pos.x % offset);
    int currentCol2 = node2Pos.x - (node2Pos.x % offset);

    return currentRow1 == currentRow2 && currentCol1 == currentCol2;
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
                .toList();

        Optional<EntrancePoint> firstEntrance = entrancesInClusterGroup.stream().findFirst();
        if (firstEntrance.isEmpty()) {
          continue;
        }

        CustomVec2D entrancePosition =
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
    IdPath<AbstractNode> idPath = search.findPath();
    assert idPath != null;
    if (idPath.pathCost >= 0) {
      addEdge(
          srcAbstractNodeId,
          destAbstractNodeId,
          idPath.pathCost,
          level,
          false,
          new ArrayList<>(idPath.pathNodes));
      Collections.reverse(idPath.pathNodes);
      addEdge(
          destAbstractNodeId, srcAbstractNodeId, idPath.pathCost, level, false, idPath.pathNodes);
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

  @Override
  public String toString() {
    int nrClustersPerRow =
        size.width % clusterSize == 0 ? size.width / clusterSize : size.width / clusterSize + 1;
    ColoredStringBuilder csb = new ColoredStringBuilder();

    for (int y = 0; y < size.height; y++) {
      int relY = y % clusterSize;
      if (relY == 0) {
        horizontalClusterBorder(csb, y);
        csb.newLine();
      }
      for (int x = 0; x < size.width; x++) {
        CustomVec2D pos = new CustomVec2D(x, y);
        int relX = x % clusterSize;
        CustomVec2D relPos = new CustomVec2D(relX, relY);
        if (relX == 0) {
          verticalClusterBorderEdge(csb, pos, relPos);
        }

        nodeToString(csb, pos, relPos);
      }
      csb.append('|').newLine();
    }
    csb.append("-".repeat(size.width + nrClustersPerRow + 1));
    return csb.toString();
  }

  private void nodeToString(ColoredStringBuilder csb, CustomVec2D pos, CustomVec2D relPos) {
    Cluster cluster = findClusterForPosition(pos);
    Id<ConcreteNode> nodeId = cluster.subConcreteMap.getNodeIdFromPos(relPos);
    ConcreteNode node = cluster.subConcreteMap.graph.getNode(nodeId);
    Color color;
    if (cluster.subConcreteMap.passability.cannotEnter(relPos)) {
      color = Color.TRANSPARENT;
    } else if (cluster.subConcreteMap.passability.canMoveDiagonal(relPos)) {
      color = Color.GREEN_BRIGHT;
    } else {
      color = Color.CYAN_BRIGHT;
    }
    csb.append(color, node.edges.size());
  }

  private void verticalClusterBorderEdge(
      ColoredStringBuilder csb, CustomVec2D pos, CustomVec2D relPos) {
    Cluster cluster = findClusterForPosition(pos);
    CustomVec2D posLeft = new CustomVec2D(pos.x - 1, pos.y);

    List<AbstractNode> absNodes =
        cluster.entrancePoints.stream()
            .filter(entrancePoint -> entrancePoint.relativePosition.equals(relPos))
            .map(entrancePoint -> abstractGraph.getNode(entrancePoint.abstractNodeId))
            .toList();
    for (AbstractNode absNode : absNodes) {
      for (Id<AbstractNode> neighbourNodeId : absNode.edges.keySet()) {
        CustomVec2D neighbourPos = abstractGraph.getNodeInfo(neighbourNodeId).position;
        if (neighbourPos.x != pos.x - 1) {
          continue;
        }

        if (neighbourPos.y == pos.y) {
          csb.append(Color.RED, '-');
          return;
        } else if (neighbourPos.y == pos.y - 1) {
          csb.append(Color.RED, '/');
          return;
        } else if (neighbourPos.y == pos.y + 1) {
          csb.append(Color.RED, '\\');
          return;
        }
      }
    }

    csb.append('|');
  }

  private void horizontalClusterBorder(ColoredStringBuilder csb, int y) {
    int relY = y % clusterSize;

    outerloop:
    for (int x = 0; x < size.width; x++) {
      int relX = x % clusterSize;
      CustomVec2D absPos = new CustomVec2D(x, y);
      Cluster cluster = findClusterForPosition(absPos);
      if (relX == 0) {
        csb.append(Color.MAGENTA_BRIGHT, cluster.entrancePoints.size());
      }

      if (y - 1 < 0) {
        csb.append('-');
      } else {
        CustomVec2D posAbove = new CustomVec2D(x, y - 1);
        CustomVec2D relPos = new CustomVec2D(relX, relY);
        Cluster clusterAbove = findClusterForPosition(posAbove);

        List<AbstractNode> absNodes =
            cluster.entrancePoints.stream()
                .filter(entrancePoint -> entrancePoint.relativePosition.equals(relPos))
                .map(entrancePoint -> abstractGraph.getNode(entrancePoint.abstractNodeId))
                .toList();
        for (AbstractNode absNode : absNodes) {
          for (Id<AbstractNode> neighbourNodeId : absNode.edges.keySet()) {
            CustomVec2D neighbourPos = abstractGraph.getNodeInfo(neighbourNodeId).position;
            if (neighbourPos.y != y - 1) {
              continue;
            }

            if (neighbourPos.x == x) {
              csb.append(Color.RED, '|');
              continue outerloop;
            } else if (neighbourPos.x == x - 1) {
              csb.append(Color.RED, '\\');
              continue outerloop;
            } else if (neighbourPos.x == x + 1) {
              csb.append(Color.RED, '/');
              continue outerloop;
            }
          }
        }

        csb.append('-');
      }
    }
    csb.append('-');
  }
}
