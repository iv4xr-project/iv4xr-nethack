package agent.navigation.hpastar.factories;

import agent.AgentLoggers;
import agent.navigation.hpastar.*;
import agent.navigation.hpastar.graph.*;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.utils.RefSupport;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.Logger;

public class HierarchicalMapFactory {
  static final Logger hpaLogger = AgentLoggers.HPALogger;
  private final int MAX_ENTRANCE_WIDTH = 6;

  public HierarchicalMap hierarchicalMap;

  public ConcreteMap concreteMap;

  private EntranceStyle entranceStyle;

  private int clusterSize;

  private int maxLevel;

  final Map<Id<AbstractNode>, NodeBackup> nodeBackups = new HashMap<>();

  public HierarchicalMap createHierarchicalMap(
      ConcreteMap concreteMap, int clusterSize, int maxLevel, EntranceStyle style, Size size) {
    this.clusterSize = clusterSize;
    entranceStyle = style;
    this.maxLevel = maxLevel;
    this.concreteMap = concreteMap;
    hierarchicalMap = new HierarchicalMap(TileType.OctileUnicost, clusterSize, maxLevel, size);
    List<Entrance> entrances = new ArrayList<>();
    createEntrancesAndClusters(entrances);
    createAbstractNodes(entrances);
    createEdges(entrances);
    return hierarchicalMap;
  }

  public void removeAbstractNode(HierarchicalMap map, Id<AbstractNode> nodeId) {
    if (nodeBackups.containsKey(nodeId)) {
      restoreNodeBackup(map, nodeId);
    } else {
      map.removeAbstractNode(nodeId);
    }
  }

  public Id<AbstractNode> insertAbstractNode(HierarchicalMap map, IntVec2D pos) {
    Id<ConcreteNode> nodeId = new Id<ConcreteNode>().from(pos.y * map.size.width + pos.x);
    Id<AbstractNode> abstractNodeId = insertNodeIntoHierarchicalMap(map, nodeId, pos);
    map.addHierarchicalEdgesForAbstractNode(abstractNodeId);
    return abstractNodeId;
  }

  //  insert a new node, such as start or target, to the abstract graph and
  //  returns the id of the newly created node in the abstract graph
  //  x and y are the positions where I want to put the node
  public Id<AbstractNode> insertNodeIntoHierarchicalMap(
      HierarchicalMap map, Id<ConcreteNode> concreteNodeId, IntVec2D pos) {
    //  If the node already existed (for instance, it was an entrance point already
    //  existing in the graph, we need to keep track of the previous status in order
    //  to be able to restore it once we delete this STAL
    if (map.concreteNodeIdToAbstractNodeIdMap.containsKey(concreteNodeId)) {
      Id<AbstractNode> existingAbstractNodeId =
          map.concreteNodeIdToAbstractNodeIdMap.get(concreteNodeId);
      NodeBackup nodeBackup =
          new NodeBackup(
              map.abstractGraph.getNodeInfo(existingAbstractNodeId).level,
              map.getNodeEdges(concreteNodeId));
      nodeBackups.put(existingAbstractNodeId, nodeBackup);
      return map.concreteNodeIdToAbstractNodeIdMap.get(concreteNodeId);
    }

    Cluster cluster = map.findClusterForPosition(pos);
    //  create global entrance
    Id<AbstractNode> abstractNodeId = new Id<AbstractNode>().from(map.getNrNodes());
    var entrance =
        cluster.addEntrance(
            abstractNodeId, new IntVec2D(pos.x - cluster.origin.x, pos.y - cluster.origin.y));
    cluster.updatePathsForLocalEntrance(entrance);
    map.concreteNodeIdToAbstractNodeIdMap.put(concreteNodeId, abstractNodeId);
    AbstractNodeInfo info =
        new AbstractNodeInfo(abstractNodeId, 1, cluster.id, pos, concreteNodeId);
    //    System.out.printf("AbsGraph AddNode: %s%n", abstractNodeId);
    boolean edgeAdded = false;
    map.abstractGraph.addNode(abstractNodeId, info);
    for (EntrancePoint entrancePoint : cluster.entrancePoints) {
      if (cluster.areConnected(abstractNodeId, entrancePoint.abstractNodeId)) {
        map.addEdge(
            entrancePoint.abstractNodeId,
            abstractNodeId,
            cluster.getDistance(entrancePoint.abstractNodeId, abstractNodeId));
        map.addEdge(
            abstractNodeId,
            entrancePoint.abstractNodeId,
            cluster.getDistance(abstractNodeId, entrancePoint.abstractNodeId));

        edgeAdded = true;
      }
    }

    return abstractNodeId;
  }

  private void restoreNodeBackup(HierarchicalMap map, Id<AbstractNode> nodeId) {
    var abstractGraph = map.abstractGraph;
    var nodeBackup = nodeBackups.get(nodeId);
    var nodeInfo = abstractGraph.getNodeInfo(nodeId);
    nodeInfo.level = nodeBackup.level;
    abstractGraph.removeEdgesFromAndToNode(nodeId);

    // Updates node
    abstractGraph.addNode(nodeId, nodeInfo);
    for (AbstractEdge edge : nodeBackup.edges) {
      var targetNodeId = edge.targetNodeId;
      map.addEdge(
          nodeId,
          targetNodeId,
          edge.info.cost,
          edge.info.level,
          edge.info.isInterClusterEdge,
          edge.info.innerLowerLevelPath != null
              ? new ArrayList<>(edge.info.innerLowerLevelPath)
              : null);
      // TODO: Warning!!!, inline IF is not supported ?
      if (edge.info.innerLowerLevelPath != null) {
        Collections.reverse(edge.info.innerLowerLevelPath);
      }
      map.addEdge(
          targetNodeId,
          nodeId,
          edge.info.cost,
          edge.info.level,
          edge.info.isInterClusterEdge,
          edge.info.innerLowerLevelPath);
    }

    nodeBackups.remove(nodeId);
  }

  private void createEdges(List<Entrance> entrances) {
    for (Entrance entrance : entrances) {
      createEntranceEdges(entrance, hierarchicalMap.type);
    }

    for (Cluster cluster : hierarchicalMap.clusters) {
      cluster.createIntraClusterEdges();
      createIntraClusterEdges(cluster);
    }

    hierarchicalMap.createHierarchicalEdges();
  }

  private void createEntranceEdges(Entrance entrance, AbsType type) {
    var level = entrance.getEntranceLevel(clusterSize, maxLevel);
    var srcAbstractNodeId =
        hierarchicalMap.concreteNodeIdToAbstractNodeIdMap.get(entrance.srcNode.nodeId);
    var destAbstractNodeId =
        hierarchicalMap.concreteNodeIdToAbstractNodeIdMap.get(entrance.destNode.nodeId);
    var orientation = entrance.orientation;
    int cost = Constants.COST_ONE;
    switch (type) {
      case ABSTRACT_TILE:
      case ABSTRACT_OCTILE_UNICOST:
        //  Inter-edges: cost 1
        cost = Constants.COST_ONE;
        break;
      case ABSTRACT_OCTILE:
        int unitCost;
        switch (orientation) {
          case Horizontal:
          case Vertical:
            unitCost = Constants.COST_ONE;
            break;
          case Hdiag2:
          case Hdiag1:
          case Vdiag1:
          case Vdiag2:
            unitCost = ((Constants.COST_ONE * 34) / 24);
            break;
          default:
            unitCost = -1;
            break;
        }
        cost = unitCost;
        break;
    }
    hpaLogger.trace(
        String.format("InterCluster AddEdge: %s -> %s", srcAbstractNodeId, destAbstractNodeId));
    hierarchicalMap.abstractGraph.addEdge(
        srcAbstractNodeId, destAbstractNodeId, new AbstractEdgeInfo(cost, level, true));
    hierarchicalMap.abstractGraph.addEdge(
        destAbstractNodeId, srcAbstractNodeId, new AbstractEdgeInfo(cost, level, true));
  }

  private void createIntraClusterEdges(Cluster cluster) {
    for (EntrancePoint point1 : cluster.entrancePoints) {
      for (EntrancePoint point2 : cluster.entrancePoints) {
        if (!point1.equals(point2)
            && cluster.areConnected(point1.abstractNodeId, point2.abstractNodeId)) {
          AbstractEdgeInfo abstractEdgeInfo =
              new AbstractEdgeInfo(
                  cluster.getDistance(point1.abstractNodeId, point2.abstractNodeId), 1, false);
          System.out.printf(
              "IntraCluster AddEdge: %s -> %s%n", point1.abstractNodeId, point2.abstractNodeId);
          hierarchicalMap.abstractGraph.addEdge(
              point1.abstractNodeId, point2.abstractNodeId, abstractEdgeInfo);
        }
      }
    }
  }

  private void createEntrancesAndClusters(List<Entrance> entrances) {
    int clusterId = 0;
    var entranceId = 0;
    for (int top = 0, clusterY = 0; top < concreteMap.size.height; top += clusterSize, clusterY++) {
      for (int left = 0, clusterX = 0;
          left < concreteMap.size.width;
          left += clusterSize, clusterX++) {
        int width = Math.min(clusterSize, concreteMap.size.width - left);
        int height = Math.min(clusterSize, concreteMap.size.height - top);

        Cluster cluster =
            new Cluster(
                concreteMap,
                new Id<Cluster>().from(clusterId),
                clusterX,
                clusterY,
                new IntVec2D(left, top),
                new Size(width, height));

        hierarchicalMap.clusters.add(cluster);
        clusterId++;
        Cluster clusterAbove =
            top > 0 ? getCluster(hierarchicalMap.clusters, clusterX, clusterY - 1) : null;
        Cluster clusterOnLeft =
            left > 0 ? getCluster(hierarchicalMap.clusters, clusterX - 1, clusterY) : null;
        entrances.addAll(
            createInterClusterEntrances(
                cluster, clusterAbove, clusterOnLeft, new RefSupport<>(entranceId)));
      }
    }
  }

  private List<Entrance> createInterClusterEntrances(
      Cluster cluster,
      Cluster clusterAbove,
      Cluster clusterOnLeft,
      RefSupport<Integer> entranceId) {
    List<Entrance> entrances = new ArrayList<>();
    int top = cluster.origin.y;
    int left = cluster.origin.x;
    if (clusterAbove != null) {
      List<Entrance> entrancesOnTop =
          createEntrancesOnTop(
              left, left + (cluster.size.width - 1), top - 1, clusterAbove, cluster, entranceId);
      entrances.addAll(entrancesOnTop);
    }

    if (clusterOnLeft != null) {
      List<Entrance> entrancesOnLeft =
          createEntrancesOnLeft(
              top, top + (cluster.size.height - 1), left - 1, clusterOnLeft, cluster, entranceId);
      entrances.addAll(entrancesOnLeft);
    }

    return entrances;
  }

  private List<Entrance> createEntrancesOnLeft(
      int rowStart,
      int rowEnd,
      int column,
      Cluster clusterOnLeft,
      Cluster cluster,
      RefSupport<Integer> currentEntranceId) {
    Function<Integer, Pair<ConcreteNode, ConcreteNode>> getNodesForRow =
        row -> new Pair<>(getNode(column, row), getNode(column + 1, row));
    return createEntrancesAlongEdge(
        rowStart,
        rowEnd,
        clusterOnLeft,
        cluster,
        currentEntranceId,
        getNodesForRow,
        Orientation.Horizontal);
  }

  private List<Entrance> createEntrancesOnTop(
      int colStart,
      int colEnd,
      int row,
      Cluster clusterOnTop,
      Cluster cluster,
      RefSupport<Integer> currentEntranceId) {
    Function<Integer, Pair<ConcreteNode, ConcreteNode>> getNodesForColumn =
        (column) -> new Pair<>(getNode(column, row), getNode(column, row + 1));
    return createEntrancesAlongEdge(
        colStart,
        colEnd,
        clusterOnTop,
        cluster,
        currentEntranceId,
        getNodesForColumn,
        Orientation.Vertical);
  }

  private List<Entrance> createEntrancesAlongEdge(
      int startPoint,
      int endPoint,
      Cluster precedentCluster,
      Cluster currentCluster,
      RefSupport<Integer> currentEntranceId,
      Function<Integer, Pair<ConcreteNode, ConcreteNode>> getNodesInEdge,
      Orientation orientation) {
    List<Entrance> entrances = new ArrayList<>();
    for (int entranceStart = startPoint; entranceStart <= endPoint; entranceStart++) {
      int size = getEntranceSize(entranceStart, endPoint, getNodesInEdge);
      int entranceEnd = entranceStart + (size - 1);
      if (size == 0) {
        continue;
      }

      if (entranceStyle == EntranceStyle.EndEntrance && size > MAX_ENTRANCE_WIDTH) {
        Pair<ConcreteNode, ConcreteNode> nodes = getNodesInEdge.apply(entranceStart);
        ConcreteNode srcNode = nodes.fst;
        ConcreteNode destNode = nodes.snd;
        Entrance entrance1 =
            new Entrance(
                new Id<Entrance>().from(currentEntranceId.getValue()),
                precedentCluster,
                currentCluster,
                srcNode,
                destNode,
                orientation);
        currentEntranceId.setValue(currentEntranceId.getValue() + 1);
        nodes = getNodesInEdge.apply(entranceEnd);
        srcNode = nodes.fst;
        destNode = nodes.snd;
        Entrance entrance2 =
            new Entrance(
                new Id<Entrance>().from(currentEntranceId.getValue()),
                precedentCluster,
                currentCluster,
                srcNode,
                destNode,
                orientation);
        currentEntranceId.setValue(currentEntranceId.getValue() + 1);
        entrances.add(entrance1);
        entrances.add(entrance2);
        System.out.printf(
            "1. Entrance added (%s -> %s) between cluster %s -> %s%n",
            srcNode.info.position, destNode.info.position, precedentCluster, currentCluster);
      } else {
        Pair<ConcreteNode, ConcreteNode> nodes =
            getNodesInEdge.apply((entranceEnd + entranceStart) / 2);
        ConcreteNode srcNode = nodes.fst;
        ConcreteNode destNode = nodes.snd;
        Entrance entrance =
            new Entrance(
                new Id<Entrance>().from(currentEntranceId.getValue()),
                precedentCluster,
                currentCluster,
                srcNode,
                destNode,
                orientation);
        currentEntranceId.setValue(currentEntranceId.getValue() + 1);
        entrances.add(entrance);
        System.out.printf(
            "2. Entrance added (%s -> %s) between cluster %s -> %s%n",
            srcNode.info.position, destNode.info.position, precedentCluster, currentCluster);
      }

      entranceStart = entranceEnd;
    }

    return entrances;
  }

  private int getEntranceSize(
      int entranceStart,
      int end,
      Function<Integer, Pair<ConcreteNode, ConcreteNode>> getNodesInEdge) {
    var size = 0;
    while (entranceStart + size <= end
        && !entranceIsBlocked(entranceStart + size, getNodesInEdge)) {
      size++;
    }

    return size;
  }

  private ConcreteNode getNode(int left, int top) {
    return concreteMap.graph.getNode(concreteMap.getNodeIdFromPos(left, top));
  }

  private boolean entranceIsBlocked(
      int entrancePoint, Function<Integer, Pair<ConcreteNode, ConcreteNode>> getNodesInEdge) {
    var nodes = getNodesInEdge.apply(entrancePoint);
    return nodes.fst.info.isObstacle || nodes.snd.info.isObstacle;
  }

  private Cluster getCluster(List<Cluster> clusters, int left, int top) {
    var clustersW = hierarchicalMap.size.width / clusterSize;
    if (hierarchicalMap.size.width % clusterSize > 0) {
      clustersW++;
    }

    return clusters.get(top * clustersW + left);
  }

  private void createAbstractNodes(List<Entrance> entrancesList) {
    Iterable<AbstractNodeInfo> abstractNodes = generateAbstractNodes(entrancesList);
    for (AbstractNodeInfo abstractNode : abstractNodes) {
      hierarchicalMap.concreteNodeIdToAbstractNodeIdMap.put(
          abstractNode.concreteNodeId, abstractNode.id);
      hierarchicalMap.abstractGraph.addNode(abstractNode.id, abstractNode);
    }
  }

  private Iterable<AbstractNodeInfo> generateAbstractNodes(List<Entrance> entrances) {
    RefSupport<Integer> abstractNodeId = new RefSupport<>(0);
    Map<Id<ConcreteNode>, AbstractNodeInfo> abstractNodesDict = new HashMap<>();
    for (Entrance entrance : entrances) {
      int level = entrance.getEntranceLevel(clusterSize, maxLevel);
      createOrUpdateAbstractNodeFromConcreteNode(
          entrance.srcNode, entrance.cluster1, abstractNodeId, level, abstractNodesDict);
      createOrUpdateAbstractNodeFromConcreteNode(
          entrance.destNode, entrance.cluster2, abstractNodeId, level, abstractNodesDict);
    }

    return abstractNodesDict.values().stream()
        .sorted((node1, node2) -> Integer.compare(node1.id.getIdValue(), node2.id.getIdValue()))
        .collect(Collectors.toList());
  }

  private void createOrUpdateAbstractNodeFromConcreteNode(
      ConcreteNode srcNode,
      Cluster cluster,
      RefSupport<Integer> abstractNodeId,
      int level,
      Map<Id<ConcreteNode>, AbstractNodeInfo> abstractNodes) {
    AbstractNodeInfo abstractNodeInfo;
    if (!abstractNodes.containsKey(srcNode.nodeId)) {
      IntVec2D relativePosition =
          new IntVec2D(
              srcNode.info.position.x - cluster.origin.x,
              srcNode.info.position.y - cluster.origin.y);
      assert relativePosition.x >= 0 && relativePosition.y >= 0;
      cluster.addEntrance(new Id<AbstractNode>().from(abstractNodeId.getValue()), relativePosition);
      abstractNodeInfo =
          new AbstractNodeInfo(
              new Id<AbstractNode>().from(abstractNodeId.getValue()),
              level,
              cluster.id,
              srcNode.info.position,
              srcNode.nodeId);
      abstractNodes.put(srcNode.nodeId, abstractNodeInfo);
      abstractNodeId.setValue(abstractNodeId.getValue() + 1);
      return;
    }
    abstractNodeInfo = abstractNodes.get(srcNode.nodeId);
    if (level > abstractNodeInfo.level) {
      abstractNodeInfo.level = level;
    }
  }
}
