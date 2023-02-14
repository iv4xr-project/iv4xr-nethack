package agent.navigation;

import agent.navigation.hpastar.Cluster;
import agent.navigation.hpastar.ConcreteMap;
import agent.navigation.hpastar.EntrancePoint;
import agent.navigation.hpastar.Orientation;
import agent.navigation.hpastar.factories.Entrance;
import agent.navigation.hpastar.factories.EntranceStyle;
import agent.navigation.hpastar.factories.GraphFactory;
import agent.navigation.hpastar.graph.*;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.smoother.Direction;
import agent.navigation.hpastar.utils.RefSupport;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Tile;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import nl.uu.cs.aplib.utils.Pair;

public class GridSurfaceFactory {
  static GridSurface staticSurface;
  static final int MAX_ENTRANCE_WIDTH = 6;
  static final EntranceStyle entranceStyle = EntranceStyle.EndEntrance;

  public static Set<Direction> removeEdges(GridSurface surface, Tile tile) {
    staticSurface = surface;
    Cluster originalCluster = staticSurface.hierarchicalMap.findClusterForPosition(tile.pos);
    ConcreteMap subConcreteMap = originalCluster.subConcreteMap;
    IntVec2D relativePos = staticSurface.toRelativePos(tile.pos);

    Id<ConcreteNode> nodeId =
        GraphFactory.getNodeByPos(
                subConcreteMap.graph, relativePos, staticSurface.hierarchicalMap.clusterSize)
            .nodeId;
    ConcreteNode node = subConcreteMap.graph.getNode(nodeId);
    node.info.isObstacle = true;
    subConcreteMap.graph.removeEdgesFromAndToNode(nodeId);

    return new HashSet<>();
  }

  public static Set<Direction> addEdges(GridSurface surface, Tile t) {
    staticSurface = surface;
    Set<Direction> updateClusterEdges = new HashSet<>();

    Cluster originalCluster = staticSurface.hierarchicalMap.findClusterForPosition(t.pos);
    ConcreteMap subConcreteMap = originalCluster.subConcreteMap;
    IntVec2D relativePos = staticSurface.toRelativePos(t.pos);

    Id<ConcreteNode> nodeId =
        GraphFactory.getNodeByPos(
                subConcreteMap.graph, relativePos, staticSurface.hierarchicalMap.clusterSize)
            .nodeId;
    ConcreteNode node = subConcreteMap.graph.getNode(nodeId);
    node.info.isObstacle = false;

    List<IntVec2D> neighbours =
        NavUtils.neighbourCoordinates(t.pos, staticSurface.hierarchicalMap.size, true);
    for (IntVec2D neighbourPos : neighbours) {
      Cluster neighbourCluster = staticSurface.hierarchicalMap.findClusterForPosition(neighbourPos);
      IntVec2D neighbourRelativePos = staticSurface.toRelativePos(neighbourPos);
      ConcreteMap neighbourConcreteMap = neighbourCluster.subConcreteMap;
      if (neighbourConcreteMap.passability.cannotEnter(neighbourRelativePos, new RefSupport<>())) {
        continue; // Do not add edge if it is not passable
      }

      if (NavUtils.isDiagonal(t.pos, neighbourPos)) {
        // Cannot move diagonal
        if (!subConcreteMap.passability.canMoveDiagonal(relativePos)
            || !neighbourConcreteMap.passability.canMoveDiagonal(neighbourRelativePos)) {
          continue;
        }
      }
      if (!originalCluster.id.equals(neighbourCluster.id)) {
        if (originalCluster.clusterX < neighbourCluster.clusterX) {
          updateClusterEdges.add(Direction.East);
        } else if (originalCluster.clusterX > neighbourCluster.clusterX) {
          updateClusterEdges.add(Direction.West);
        } else if (originalCluster.clusterY < neighbourCluster.clusterY) {
          updateClusterEdges.add(Direction.South);
        } else {
          updateClusterEdges.add(Direction.North);
        }
      } else {
        Id<ConcreteNode> neighBourId =
            GraphFactory.getNodeByPos(
                    neighbourConcreteMap.graph,
                    neighbourRelativePos,
                    staticSurface.hierarchicalMap.clusterSize)
                .nodeId;
        subConcreteMap.graph.addEdge(nodeId, neighBourId, new ConcreteEdgeInfo(Constants.COST_ONE));
        subConcreteMap.graph.addEdge(neighBourId, nodeId, new ConcreteEdgeInfo(Constants.COST_ONE));
      }
    }

    assert node.edges.size() <= 8
        : "Node should not have more than 8 edges but has " + node.edges.size();
    return updateClusterEdges;
  }

  public static void createIntraClusterEdges(GridSurface surface, Cluster cluster) {
    GridSurfaceFactory.staticSurface = surface;
    for (EntrancePoint point1 : cluster.entrancePoints) {
      for (EntrancePoint point2 : cluster.entrancePoints) {
        if (!point1.equals(point2)
            && cluster.areConnected(point1.abstractNodeId, point2.abstractNodeId)) {
          AbstractEdgeInfo abstractEdgeInfo =
              new AbstractEdgeInfo(
                  cluster.getDistance(point1.abstractNodeId, point2.abstractNodeId), 1, false);
          System.out.printf(
              "IntraCluster AddEdge: %s -> %s%n", point1.abstractNodeId, point2.abstractNodeId);
          staticSurface.hierarchicalMap.abstractGraph.addEdge(
              point1.abstractNodeId, point2.abstractNodeId, abstractEdgeInfo);
        }
      }
    }
  }

  private static void addInterClusterEdge(Entrance entrance) {
    IntVec2D srcPos =
        new IntVec2D(
            entrance.cluster1.origin.x + entrance.srcNode.info.position.x,
            entrance.cluster1.origin.y + entrance.srcNode.info.position.y);
    IntVec2D destPos =
        new IntVec2D(
            entrance.cluster2.origin.x + entrance.destNode.info.position.x,
            entrance.cluster2.origin.y + entrance.destNode.info.position.y);
    Id<AbstractNode> startAbsNodeId = addAbstractNode(entrance.cluster1, srcPos);
    Id<AbstractNode> targetAbsNodeId = addAbstractNode(entrance.cluster2, destPos);

    System.out.printf(
        "InterCluster AddEdge: %s (%s) -> %s (%s)%n",
        startAbsNodeId, srcPos, targetAbsNodeId, destPos);
    staticSurface.hierarchicalMap.abstractGraph.addEdge(
        startAbsNodeId, targetAbsNodeId, new AbstractEdgeInfo(Constants.COST_ONE, 1, true));
    staticSurface.hierarchicalMap.abstractGraph.addEdge(
        targetAbsNodeId, startAbsNodeId, new AbstractEdgeInfo(Constants.COST_ONE, 1, true));
  }

  private static Id<AbstractNode> addAbstractNode(Cluster cluster, IntVec2D pos) {
    Id<AbstractNode> absNodeId =
        new Id<AbstractNode>().from(staticSurface.hierarchicalMap.abstractGraph.nextId);
    cluster.addEntrance(absNodeId, staticSurface.toRelativePos(pos));
    var nodeId = staticSurface.concreteMap.getNodeIdFromPos(pos);
    AbstractNodeInfo nodeInfo = new AbstractNodeInfo(absNodeId, 1, cluster.id, pos, nodeId);
    staticSurface.hierarchicalMap.concreteNodeIdToAbstractNodeIdMap.put(nodeId, nodeInfo.id);
    staticSurface.hierarchicalMap.abstractGraph.addNode(nodeInfo.id, nodeInfo);
    return absNodeId;
  }

  public static List<Entrance> createEntrancesOnLeft(
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

  public static List<Entrance> createEntrancesOnTop(
      int colStart,
      int colEnd,
      int row,
      Cluster clusterOnTop,
      Cluster cluster,
      RefSupport<Integer> currentEntranceId) {
    Function<Integer, Pair<ConcreteNode, ConcreteNode>> getNodesForColumn =
        column -> new Pair<>(getNode(column, row), getNode(column, row + 1));

    return createEntrancesAlongEdge(
        colStart,
        colEnd,
        clusterOnTop,
        cluster,
        currentEntranceId,
        getNodesForColumn,
        Orientation.Vertical);
  }

  private static List<Entrance> createEntrancesAlongEdge(
      int startPoint,
      int endPoint,
      Cluster precedentCluster,
      Cluster currentCluster,
      RefSupport<Integer> currentEntranceId,
      Function<Integer, Pair<ConcreteNode, ConcreteNode>> getNodesInEdge,
      Orientation orientation) {
    List<Entrance> entrances = new ArrayList<>();
    removeEntrancesBetweenClusters(precedentCluster, currentCluster);

    for (int entranceStart = startPoint; entranceStart <= endPoint; entranceStart++) {
      int size = getEntranceSize(entranceStart, endPoint, getNodesInEdge);
      int entranceEnd = entranceStart + size - 1;
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
        //        System.out.printf(
        //            "1. Entrance added relPos:(%s -> %s) between cluster %s -> %s%n",
        //            srcNode.info.position, destNode.info.position, precedentCluster,
        // currentCluster);
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
        //        System.out.printf(
        //            "2. Entrance added relPos:(%s -> %s) between cluster %s -> %s%n",
        //            srcNode.info.position, destNode.info.position, precedentCluster,
        // currentCluster);
      }

      entranceStart = entranceEnd;
    }

    for (Entrance entrance : entrances) {
      addInterClusterEdge(entrance);
    }

    return entrances;
  }

  private static void removeEntrancesBetweenClusters(Cluster cluster1, Cluster cluster2) {
    List<EntrancePoint> removeFromCluster = new ArrayList<>();
    for (EntrancePoint entrance1 : cluster1.entrancePoints) {
      AbstractNode absNode1 =
          staticSurface.hierarchicalMap.abstractGraph.getNode(entrance1.abstractNodeId);
      if (absNode1 == null) {
        continue;
      }
      for (EntrancePoint entrance2 : cluster2.entrancePoints) {
        AbstractNode absNode2 =
            staticSurface.hierarchicalMap.abstractGraph.getNode(entrance2.abstractNodeId);
        if (absNode2 == null) {
          continue;
        }

        IntVec2D entrance1Pos = absNode1.info.position;
        IntVec2D entrance2Pos = absNode2.info.position;
        if (NavUtils.adjacent(entrance1Pos, entrance2Pos, true)
            && absNode1.edges.containsKey(absNode2.nodeId)) {
          removeFromCluster.add(entrance1);
          removeFromCluster.add(entrance2);
          absNode1.removeEdge(absNode2.nodeId);
          absNode2.removeEdge(absNode1.nodeId);

          if (absNode1.edges.isEmpty()) {
            staticSurface.hierarchicalMap.abstractGraph.removeNode(absNode1.nodeId);
          }
          if (absNode2.edges.isEmpty()) {
            staticSurface.hierarchicalMap.abstractGraph.removeNode(absNode2.nodeId);
          }
        }
      }
    }

    cluster1.entrancePoints.removeAll(removeFromCluster);
    cluster2.entrancePoints.removeAll(removeFromCluster);
  }

  public static Cluster getClusterInDirection(
      GridSurface surface, Cluster cluster, Direction direction) {
    staticSurface = surface;
    int clustersW =
        staticSurface.hierarchicalMap.size.width / staticSurface.hierarchicalMap.clusterSize;
    if (staticSurface.hierarchicalMap.size.width % staticSurface.hierarchicalMap.clusterSize != 0) {
      clustersW++;
    }

    if (direction == Direction.North) {
      return staticSurface.hierarchicalMap.clusters.get(cluster.id.getIdValue() - clustersW);
    } else if (direction == Direction.West) {
      return staticSurface.hierarchicalMap.clusters.get(cluster.id.getIdValue() - 1);
    } else if (direction == Direction.South) {
      return staticSurface.hierarchicalMap.clusters.get(cluster.id.getIdValue() + clustersW);
    } else if (direction == Direction.East) {
      return staticSurface.hierarchicalMap.clusters.get(cluster.id.getIdValue() + 1);
    } else {
      throw new RuntimeException("Cluster direction is invalid");
    }
  }

  private static int getEntranceSize(
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

  private static ConcreteNode getNode(int left, int top) {
    IntVec2D pos = new IntVec2D(left, top);
    Cluster cluster = staticSurface.hierarchicalMap.findClusterForPosition(pos);
    IntVec2D relPos = staticSurface.toRelativePos(pos);
    var nodeId = cluster.subConcreteMap.getNodeIdFromPos(relPos);
    var node = cluster.subConcreteMap.graph.getNode(nodeId);
    return node;
  }

  private static boolean entranceIsBlocked(
      int entrancePoint, Function<Integer, Pair<ConcreteNode, ConcreteNode>> getNodesInEdge) {
    var nodes = getNodesInEdge.apply(entrancePoint);
    return nodes.fst.info.isObstacle || nodes.snd.info.isObstacle;
  }
}
