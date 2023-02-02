package agent.navigation.hpastar.factories;

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

public class HierarchicalMapFactory {

  private int MAX_ENTRANCE_WIDTH = 6;

  private HierarchicalMap _hierarchicalMap;

  private ConcreteMap _concreteMap;

  private EntranceStyle _entranceStyle;

  private int _clusterSize;

  private int _maxLevel;

  Map<Id<AbstractNode>, NodeBackup> nodeBackups = new HashMap<>();

  public final HierarchicalMap createHierarchicalMap(
      ConcreteMap concreteMap, int clusterSize, int maxLevel, EntranceStyle style) {
    this._clusterSize = clusterSize;
    this._entranceStyle = style;
    this._maxLevel = maxLevel;
    this._concreteMap = concreteMap;
    this._hierarchicalMap = new HierarchicalMap(concreteMap, clusterSize, maxLevel);
    List<Entrance> entrances = new ArrayList<>();
    List<Cluster> clusters = new ArrayList<>();
    this.createEntrancesAndClusters(entrances, clusters);
    this._hierarchicalMap.clusters = clusters;
    this.createAbstractNodes(entrances);
    this.createEdges(entrances, clusters);
    return this._hierarchicalMap;
  }

  public final void removeAbstractNode(HierarchicalMap map, Id<AbstractNode> nodeId) {
    if (this.nodeBackups.containsKey(nodeId)) {
      this.restoreNodeBackup(map, nodeId);
    } else {
      map.removeAbstractNode(nodeId);
    }
  }

  public final Id<AbstractNode> insertAbstractNode(HierarchicalMap map, IntVec2D pos) {
    Id<ConcreteNode> nodeId = new Id<ConcreteNode>().from(((pos.x * map.width) + pos.x));
    Id<AbstractNode> abstractNodeId = this.insertNodeIntoHierarchicalMap(map, nodeId, pos);
    map.addHierarchicalEdgesForAbstractNode(abstractNodeId);
    return abstractNodeId;
  }

  //  insert a new node, such as start or target, to the abstract graph and
  //  returns the id of the newly created node in the abstract graph
  //  x and y are the positions where I want to put the node
  private final Id<AbstractNode> insertNodeIntoHierarchicalMap(
      HierarchicalMap map, Id<ConcreteNode> concreteNodeId, IntVec2D pos) {
    //  If the node already existed (for instance, it was the an entrance point already
    //  existing in the graph, we need to keep track of the previous status in order
    //  to be able to restore it once we delete this STAL
    if (map.concreteNodeIdToAbstractNodeIdMap.containsKey(concreteNodeId)) {
      Id<AbstractNode> existingAbstractNodeId =
          map.concreteNodeIdToAbstractNodeIdMap.get(concreteNodeId);
      NodeBackup nodeBackup =
          new NodeBackup(
              map.abstractGraph.getNodeInfo(existingAbstractNodeId).level,
              map.getNodeEdges(concreteNodeId));
      this.nodeBackups.put(existingAbstractNodeId, nodeBackup);
      return map.concreteNodeIdToAbstractNodeIdMap.get(concreteNodeId);
    }

    Cluster cluster = map.findClusterForPosition(pos);
    //  create global entrance
    Id<AbstractNode> abstractNodeId = new Id<AbstractNode>().from(map.getNrNodes());
    var entrance =
        cluster.addEntrance(
            abstractNodeId, new IntVec2D((pos.x - cluster.origin.x), (pos.y - cluster.origin.y)));
    cluster.updatePathsForLocalEntrance(entrance);
    map.concreteNodeIdToAbstractNodeIdMap.put(concreteNodeId, abstractNodeId);
    AbstractNodeInfo info =
        new AbstractNodeInfo(abstractNodeId, 1, cluster.id, pos, concreteNodeId);
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
      }
    }

    return abstractNodeId;
  }

  private final void restoreNodeBackup(HierarchicalMap map, Id<AbstractNode> nodeId) {
    var abstractGraph = map.abstractGraph;
    var nodeBackup = this.nodeBackups.get(nodeId);
    var nodeInfo = abstractGraph.getNodeInfo(nodeId);
    nodeInfo.level = nodeBackup.level;
    abstractGraph.removeEdgesFromAndToNode(nodeId);
    abstractGraph.addNode(nodeId, nodeInfo);
    for (var edge : nodeBackup.edges) {
      var targetNodeId = edge.targetNodeId;
      map.addEdge(
          nodeId,
          targetNodeId,
          edge.info.cost,
          edge.info.level,
          edge.info.isInterClusterEdge,
          new ArrayList<>(edge.info.innerLowerLevelPath));
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

    this.nodeBackups.remove(nodeId);
  }

  private final void createEdges(List<Entrance> entrances, List<Cluster> clusters) {
    for (var entrance : entrances) {
      this.createEntranceEdges(entrance, _hierarchicalMap.type);
    }

    for (var cluster : clusters) {
      cluster.createIntraClusterEdges();
      this.createIntraClusterEdges(cluster);
    }

    this._hierarchicalMap.createHierarchicalEdges();
  }

  private final void createEntranceEdges(Entrance entrance, AbsType type) {
    var level = entrance.getEntranceLevel(_clusterSize, _maxLevel);
    var srcAbstractNodeId =
        _hierarchicalMap.concreteNodeIdToAbstractNodeIdMap.get(entrance.srcNode.nodeId);
    var destAbstractNodeId =
        _hierarchicalMap.concreteNodeIdToAbstractNodeIdMap.get(entrance.destNode.nodeId);
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
    this._hierarchicalMap.abstractGraph.addEdge(
        srcAbstractNodeId, destAbstractNodeId, new AbstractEdgeInfo(cost, level, true));
    this._hierarchicalMap.abstractGraph.addEdge(
        destAbstractNodeId, srcAbstractNodeId, new AbstractEdgeInfo(cost, level, true));
  }

  private final void createIntraClusterEdges(Cluster cluster) {
    for (EntrancePoint point1 : cluster.entrancePoints) {
      for (EntrancePoint point2 : cluster.entrancePoints) {
        if (((point1 != point2)
            && cluster.areConnected(point1.abstractNodeId, point2.abstractNodeId))) {
          var abstractEdgeInfo =
              new AbstractEdgeInfo(
                  cluster.getDistance(point1.abstractNodeId, point2.abstractNodeId), 1, false);
          this._hierarchicalMap.abstractGraph.addEdge(
              point1.abstractNodeId, point2.abstractNodeId, abstractEdgeInfo);
        }
      }
    }
  }

  private final void createEntrancesAndClusters(List<Entrance> entrances, List<Cluster> clusters) {
    int clusterId = 0;
    var entranceId = 0;
    for (int top = 0, clusterY = 0;
        (top < this._concreteMap.height);
        top = (top + this._clusterSize), clusterY++) {
      for (int left = 0, clusterX = 0;
          (left < this._concreteMap.width);
          left = (left + this._clusterSize), clusterX++) {
        int width = Math.min(this._clusterSize, (this._concreteMap.width - left));
        int height = Math.min(this._clusterSize, (this._concreteMap.height - top));
        var cluster =
            new Cluster(
                this._concreteMap,
                new Id<Cluster>().from(clusterId),
                clusterX,
                clusterY,
                new IntVec2D(left, top),
                new Size(width, height));
        clusters.add(cluster);
        clusterId++;
        Cluster clusterAbove = top > 0 ? this.getCluster(clusters, clusterX, (clusterY - 1)) : null;
        Cluster clusterOnLeft =
            left > 0 ? this.getCluster(clusters, (clusterX - 1), clusterY) : null;
        entrances.addAll(
            this.createInterClusterEntrances(
                cluster, clusterAbove, clusterOnLeft, new RefSupport<>(entranceId)));
      }
    }
  }

  private final List<Entrance> createInterClusterEntrances(
      Cluster cluster,
      Cluster clusterAbove,
      Cluster clusterOnLeft,
      RefSupport<Integer> entranceId) {
    List<Entrance> entrances = new ArrayList<>();
    int top = cluster.origin.y;
    int left = cluster.origin.x;
    if ((clusterAbove != null)) {
      var entrancesOnTop =
          this.createEntrancesOnTop(
              left,
              (left + (cluster.size.width - 1)),
              (top - 1),
              clusterAbove,
              cluster,
              entranceId);
      entrances.addAll(entrancesOnTop);
    }

    if ((clusterOnLeft != null)) {
      var entrancesOnLeft =
          this.createEntrancesOnLeft(
              top,
              (top + (cluster.size.height - 1)),
              (left - 1),
              clusterOnLeft,
              cluster,
              entranceId);
      entrances.addAll(entrancesOnLeft);
    }

    return entrances;
  }

  private final List<Entrance> createEntrancesOnLeft(
      int rowStart,
      int rowEnd,
      int column,
      Cluster clusterOnLeft,
      Cluster cluster,
      RefSupport<Integer> currentEntranceId) {
    Function<Integer, Pair<ConcreteNode, ConcreteNode>> getNodesForRow =
        row -> new Pair<>(getNode(column, row), getNode((column + 1), row));
    return this.createEntrancesAlongEdge(
        rowStart,
        rowEnd,
        clusterOnLeft,
        cluster,
        currentEntranceId,
        getNodesForRow,
        Orientation.Horizontal);
  }

  private final List<Entrance> createEntrancesOnTop(
      int colStart,
      int colEnd,
      int row,
      Cluster clusterOnTop,
      Cluster cluster,
      RefSupport<Integer> currentEntranceId) {
    Function<Integer, Pair<ConcreteNode, ConcreteNode>> getNodesForColumn =
        (column) -> new Pair<>(getNode(column, row), getNode(column, (row + 1)));
    return this.createEntrancesAlongEdge(
        colStart,
        colEnd,
        clusterOnTop,
        cluster,
        currentEntranceId,
        getNodesForColumn,
        Orientation.Vertical);
  }

  private final List<Entrance> createEntrancesAlongEdge(
      int startPoint,
      int endPoint,
      Cluster precedentCluster,
      Cluster currentCluster,
      RefSupport<Integer> currentEntranceId,
      Function<Integer, Pair<ConcreteNode, ConcreteNode>> getNodesInEdge,
      Orientation orientation) {
    List<Entrance> entrances = new ArrayList<>();
    for (var entranceStart = startPoint; (entranceStart <= endPoint); entranceStart++) {
      int size = this.getEntranceSize(entranceStart, endPoint, getNodesInEdge);
      int entranceEnd = entranceStart + (size - 1);
      if (size == 0) {
        continue;
      }

      if ((this._entranceStyle == EntranceStyle.EndEntrance && (size > MAX_ENTRANCE_WIDTH))) {
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
      } else {
        Pair<ConcreteNode, ConcreteNode> nodes =
            getNodesInEdge.apply(((entranceEnd + entranceStart) / 2));
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
      }

      entranceStart = entranceEnd;
    }

    return entrances;
  }

  private final int getEntranceSize(
      int entranceStart,
      int end,
      Function<Integer, Pair<ConcreteNode, ConcreteNode>> getNodesInEdge) {
    var size = 0;
    while (entranceStart + size <= end
        && !this.entranceIsBlocked((entranceStart + size), getNodesInEdge)) {
      size++;
    }

    return size;
  }

  private final ConcreteNode getNode(int left, int top) {
    return this._concreteMap.graph.getNode(this._concreteMap.getNodeIdFromPos(left, top));
  }

  private final boolean entranceIsBlocked(
      int entrancePoint, Function<Integer, Pair<ConcreteNode, ConcreteNode>> getNodesInEdge) {
    var nodes = getNodesInEdge.apply(entrancePoint);
    return (nodes.fst.info.isObstacle || nodes.snd.info.isObstacle);
  }

  private final Cluster getCluster(List<Cluster> clusters, int left, int top) {
    var clustersW = (this._hierarchicalMap.width / this._clusterSize);
    if (((this._hierarchicalMap.width % this._clusterSize) > 0)) {
      clustersW++;
    }

    return clusters.get(((top * clustersW) + left));
  }

  private final void createAbstractNodes(List<Entrance> entrancesList) {
    Iterable<AbstractNodeInfo> abstractNodes = generateAbstractNodes(entrancesList);
    for (AbstractNodeInfo abstractNode : abstractNodes) {
      this._hierarchicalMap.concreteNodeIdToAbstractNodeIdMap.put(
          abstractNode.concreteNodeId, abstractNode.id);
      this._hierarchicalMap.abstractGraph.addNode(abstractNode.id, abstractNode);
    }
  }

  private final Iterable<AbstractNodeInfo> generateAbstractNodes(List<Entrance> entrances) {
    RefSupport<Integer> abstractNodeId = new RefSupport<>(0);
    Map<Id<ConcreteNode>, AbstractNodeInfo> abstractNodesDict = new HashMap<>();
    for (Entrance entrance : entrances) {
      var level = entrance.getEntranceLevel(this._clusterSize, this._maxLevel);
      HierarchicalMapFactory.createOrUpdateAbstractNodeFromConcreteNode(
          entrance.srcNode, entrance.cluster1, abstractNodeId, level, abstractNodesDict);
      HierarchicalMapFactory.createOrUpdateAbstractNodeFromConcreteNode(
          entrance.destNode, entrance.cluster2, abstractNodeId, level, abstractNodesDict);
    }

    return abstractNodesDict.values().stream()
        .sorted((node1, node2) -> Integer.compare(node1.id.getIdValue(), node2.id.getIdValue()))
        .collect(Collectors.toList());
  }

  private static void createOrUpdateAbstractNodeFromConcreteNode(
      ConcreteNode srcNode,
      Cluster cluster,
      RefSupport<Integer> abstractNodeId,
      int level,
      Map<Id<ConcreteNode>, AbstractNodeInfo> abstractNodes) {
    AbstractNodeInfo abstractNodeInfo;
    if (!abstractNodes.containsKey(srcNode.nodeId)) {
      cluster.addEntrance(
          new Id<AbstractNode>().from(abstractNodeId.getValue()),
          new IntVec2D(
              (srcNode.info.position.x - cluster.origin.x),
              (srcNode.info.position.y - cluster.origin.y)));
      abstractNodeInfo =
          new AbstractNodeInfo(
              new Id<AbstractNode>().from(abstractNodeId.getValue()),
              level,
              cluster.id,
              new IntVec2D(srcNode.info.position.x, srcNode.info.position.y),
              srcNode.nodeId);
      abstractNodes.put(srcNode.nodeId, abstractNodeInfo);
      abstractNodeId.setValue(abstractNodeId.getValue() + 1);
      return;
    }
    abstractNodeInfo = abstractNodes.get(srcNode.nodeId);
    if ((level > abstractNodeInfo.level)) {
      abstractNodeInfo.level = level;
    }
  }
}