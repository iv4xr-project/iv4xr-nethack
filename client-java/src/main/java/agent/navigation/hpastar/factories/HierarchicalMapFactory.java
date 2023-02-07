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

  private final int MAX_ENTRANCE_WIDTH = 6;

  private HierarchicalMap _hierarchicalMap;

  private ConcreteMap _concreteMap;

  private EntranceStyle _entranceStyle;

  private int _clusterSize;

  private int _maxLevel;

  Map<Id<AbstractNode>, NodeBackup> nodeBackups = new HashMap<>();

  public HierarchicalMap createHierarchicalMap(
      ConcreteMap concreteMap, int clusterSize, int maxLevel, EntranceStyle style) {
    _clusterSize = clusterSize;
    _entranceStyle = style;
    _maxLevel = maxLevel;
    _concreteMap = concreteMap;
    _hierarchicalMap = new HierarchicalMap(TileType.OctileUnicost, clusterSize, maxLevel);
    List<Entrance> entrances = new ArrayList<>();
    List<Cluster> clusters = new ArrayList<>();
    createEntrancesAndClusters(entrances, clusters);
    _hierarchicalMap.clusters = clusters;
    createAbstractNodes(entrances);
    createEdges(entrances, clusters);
    return _hierarchicalMap;
  }

  public void removeAbstractNode(HierarchicalMap map, Id<AbstractNode> nodeId) {
    if (nodeBackups.containsKey(nodeId)) {
      restoreNodeBackup(map, nodeId);
    } else {
      map.removeAbstractNode(nodeId);
    }
  }

  public Id<AbstractNode> insertAbstractNode(HierarchicalMap map, IntVec2D pos) {
    Id<ConcreteNode> nodeId = new Id<ConcreteNode>().from((pos.y * map.width) + pos.x);
    Id<AbstractNode> abstractNodeId = insertNodeIntoHierarchicalMap(map, nodeId, pos);
    map.addHierarchicalEdgesForAbstractNode(abstractNodeId);
    return abstractNodeId;
  }

  //  insert a new node, such as start or target, to the abstract graph and
  //  returns the id of the newly created node in the abstract graph
  //  x and y are the positions where I want to put the node
  private Id<AbstractNode> insertNodeIntoHierarchicalMap(
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

  private void restoreNodeBackup(HierarchicalMap map, Id<AbstractNode> nodeId) {
    var abstractGraph = map.abstractGraph;
    var nodeBackup = nodeBackups.get(nodeId);
    var nodeInfo = abstractGraph.getNodeInfo(nodeId);
    nodeInfo.level = nodeBackup.level;
    abstractGraph.removeEdgesFromAndToNode(nodeId);
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

  private void createEdges(List<Entrance> entrances, List<Cluster> clusters) {
    for (Entrance entrance : entrances) {
      createEntranceEdges(entrance, _hierarchicalMap.type);
    }

    for (Cluster cluster : clusters) {
      cluster.createIntraClusterEdges();
      createIntraClusterEdges(cluster);
    }

    _hierarchicalMap.createHierarchicalEdges();
  }

  private void createEntranceEdges(Entrance entrance, AbsType type) {
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
    _hierarchicalMap.abstractGraph.addEdge(
        srcAbstractNodeId, destAbstractNodeId, new AbstractEdgeInfo(cost, level, true));
    _hierarchicalMap.abstractGraph.addEdge(
        destAbstractNodeId, srcAbstractNodeId, new AbstractEdgeInfo(cost, level, true));
  }

  private void createIntraClusterEdges(Cluster cluster) {
    for (EntrancePoint point1 : cluster.entrancePoints) {
      for (EntrancePoint point2 : cluster.entrancePoints) {
        if ((!point1.equals(point2)
            && cluster.areConnected(point1.abstractNodeId, point2.abstractNodeId))) {
          var abstractEdgeInfo =
              new AbstractEdgeInfo(
                  cluster.getDistance(point1.abstractNodeId, point2.abstractNodeId), 1, false);
          _hierarchicalMap.abstractGraph.addEdge(
              point1.abstractNodeId, point2.abstractNodeId, abstractEdgeInfo);
        }
      }
    }
  }

  private void createEntrancesAndClusters(List<Entrance> entrances, List<Cluster> clusters) {
    int clusterId = 0;
    var entranceId = 0;
    for (int top = 0, clusterY = 0; top < _concreteMap.height; top += _clusterSize, clusterY++) {
      for (int left = 0, clusterX = 0;
          left < _concreteMap.width;
          left += _clusterSize, clusterX++) {
        int width = Math.min(_clusterSize, _concreteMap.width - left);
        int height = Math.min(_clusterSize, _concreteMap.height - top);
        Cluster cluster =
            new Cluster(
                _concreteMap,
                new Id<Cluster>().from(clusterId),
                clusterX,
                clusterY,
                new IntVec2D(left, top),
                new Size(width, height));
        clusters.add(cluster);
        clusterId++;
        Cluster clusterAbove = top > 0 ? getCluster(clusters, clusterX, clusterY - 1) : null;
        Cluster clusterOnLeft = left > 0 ? getCluster(clusters, clusterX - 1, clusterY) : null;
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

      if ((_entranceStyle == EntranceStyle.EndEntrance && size > MAX_ENTRANCE_WIDTH)) {
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
    return _concreteMap.graph.getNode(_concreteMap.getNodeIdFromPos(left, top));
  }

  private boolean entranceIsBlocked(
      int entrancePoint, Function<Integer, Pair<ConcreteNode, ConcreteNode>> getNodesInEdge) {
    var nodes = getNodesInEdge.apply(entrancePoint);
    return nodes.fst.info.isObstacle || nodes.snd.info.isObstacle;
  }

  private Cluster getCluster(List<Cluster> clusters, int left, int top) {
    var clustersW = _hierarchicalMap.width / _clusterSize;
    if (_hierarchicalMap.width % _clusterSize > 0) {
      clustersW++;
    }

    return clusters.get(top * clustersW + left);
  }

  private void createAbstractNodes(List<Entrance> entrancesList) {
    System.out.println(
        entrancesList.stream()
            .map(entrance -> entrance.srcNode.nodeId)
            .collect(Collectors.toList()));
    Iterable<AbstractNodeInfo> abstractNodes = generateAbstractNodes(entrancesList);
    for (AbstractNodeInfo abstractNode : abstractNodes) {
      _hierarchicalMap.concreteNodeIdToAbstractNodeIdMap.put(
          abstractNode.concreteNodeId, abstractNode.id);
      _hierarchicalMap.abstractGraph.addNode(abstractNode.id, abstractNode);
    }
  }

  private Iterable<AbstractNodeInfo> generateAbstractNodes(List<Entrance> entrances) {
    RefSupport<Integer> abstractNodeId = new RefSupport<>(0);
    Map<Id<ConcreteNode>, AbstractNodeInfo> abstractNodesDict = new HashMap<>();
    for (Entrance entrance : entrances) {
      int level = entrance.getEntranceLevel(_clusterSize, _maxLevel);
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
              new IntVec2D(srcNode.info.position.x, srcNode.info.position.y),
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
