package agent.navigation.hpastar;

import agent.navigation.GridSurface;
import agent.navigation.hpastar.graph.*;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.infrastructure.IMap;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.search.AStar;
import agent.navigation.hpastar.search.Path;
import agent.navigation.surface.Tile;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.*;
import nl.uu.cs.aplib.utils.Pair;
import util.Loggers;

public class HierarchicalGraph implements IMap<AbstractNode> {
  public final AbstractGraph abstractGraph = new AbstractGraph();
  public final Map<Pair<Integer, IntVec2D>, Id<AbstractNode>> positionToAbstractNodeIdMap =
      new HashMap<>();
  public final Map<Integer, List<IntVec2D>> levelToEntrancesMap = new HashMap<>();

  @Override
  public int getNrNodes() {
    return abstractGraph.nextId;
  }

  @Override
  public int getHeuristic(Id<AbstractNode> startNodeId, Id<AbstractNode> targetNodeId) {
    return Math.abs(
        abstractGraph.getNode(startNodeId).info.level
            - abstractGraph.getNode(targetNodeId).info.level);
  }

  /** Gets the neighbours(successors) of the nodeId for the level set in the currentLevel */
  public Iterable<Connection<AbstractNode>> getConnections(Id<AbstractNode> nodeId) {
    AbstractNode node = abstractGraph.getNode(nodeId);
    Map<Id<AbstractNode>, AbstractEdge> edges = node.edges;
    List<Connection<AbstractNode>> result = new ArrayList<>();
    for (AbstractEdge edge : edges.values()) {
      AbstractEdgeInfo edgeInfo = edge.info;
      Id<AbstractNode> targetNodeId = edge.targetNodeId;
      AbstractNodeInfo targetNodeInfo = abstractGraph.getNodeInfo(targetNodeId);
      result.add(new Connection<>(targetNodeId, edgeInfo.cost));
    }
    return result;
  }

  public Id<AbstractNode> addAbstractNode(int level, IntVec2D pos) {
    Id<AbstractNode> nodeId = getAbsNodeId(level, pos);
    if (nodeId != null) {
      System.out.printf("HierarchGraph Ignored AddNode %s: (%d,%s)%n", nodeId, level, pos);
      return nodeId;
    }

    if (!levelToEntrancesMap.containsKey(level)) {
      levelToEntrancesMap.put(level, new ArrayList<>());
    }
    levelToEntrancesMap.get(level).add(pos);

    System.out.printf("HierarchGraph AddNode (%d,%s)%n", level, pos);
    Id<AbstractNode> absNodeId = new Id<AbstractNode>().from(abstractGraph.nextId);
    AbstractNodeInfo nodeInfo = new AbstractNodeInfo(absNodeId, level, null, pos, null);
    abstractGraph.addNode(nodeInfo.id, nodeInfo);
    positionToAbstractNodeIdMap.put(new Pair<>(level, pos), absNodeId);
    return absNodeId;
  }

  public void removeAbstractNode(Id<AbstractNode> abstractNodeId) {
    AbstractNodeInfo abstractNodeInfo = abstractGraph.getNodeInfo(abstractNodeId);
    Pair<Integer, IntVec2D> pos = new Pair<>(abstractNodeInfo.level, abstractNodeInfo.position);
    positionToAbstractNodeIdMap.remove(pos);
    abstractGraph.removeEdgesFromAndToNode(abstractNodeId);
    abstractGraph.removeNode(abstractNodeId);
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
    System.out.printf("HierarchGraph AddEdge %s -> %s%n", sourceNodeId, destNodeId);
    abstractGraph.addEdge(sourceNodeId, destNodeId, edgeInfo);
  }

  public void addEdge(Id<AbstractNode> sourceNodeId, Id<AbstractNode> destNodeId, int cost) {
    addEdge(sourceNodeId, destNodeId, cost, 1, false, null);
  }

  public List<AbstractEdge> getNodeEdges(Id<AbstractNode> nodeId) {
    AbstractNode node = abstractGraph.getNode(nodeId);
    return new ArrayList<>(node.edges.values());
  }

  private static boolean isValidEdgeForLevel(AbstractEdgeInfo edgeInfo, int level) {
    if (edgeInfo.isInterClusterEdge) {
      return edgeInfo.level >= level;
    }

    return edgeInfo.level == level;
  }

  public boolean belongToSameLevel(Id<AbstractNode> node1Id, Id<AbstractNode> node2Id, int level) {
    int node1Level = abstractGraph.getNodeInfo(node1Id).level;
    int node2Level = abstractGraph.getNodeInfo(node2Id).level;
    return node1Level == node2Level;
  }

  private boolean isValidAbstractNodeForLevel(Id<AbstractNode> abstractNodeId, int level) {
    return abstractGraph.getNodeInfo(abstractNodeId).level >= level;
  }

  private int getEntrancePointLevel(EntrancePoint entrancePoint) {
    return abstractGraph.getNodeInfo(entrancePoint.abstractNodeId).level;
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

  public void createEdgesWithinLevel(int level, GridSurface surface) {
    List<IntVec2D> entrances = levelToEntrancesMap.get(level);
    if (entrances == null) {
      return;
    }

    // Remove edges between entrances within the same level
    for (IntVec2D pos : entrances) {
      Id<AbstractNode> absNodeId = getAbsNodeId(level, pos);
      List<Id<AbstractNode>> neighboursToRemove = new ArrayList<>();
      AbstractNode node = abstractGraph.getNode(absNodeId);
      var connections = getConnections(node.nodeId);
      for (var connection : connections) {
        AbstractNode neighbour = abstractGraph.getNode(connection.target);
        if (neighbour.info.level != level) {
          continue;
        }

        neighboursToRemove.add(neighbour.nodeId);
        neighbour.edges.remove(absNodeId);
      }

      for (var neighbourToRemove : neighboursToRemove) {
        node.edges.remove(neighbourToRemove);
      }
    }

    // Creates edges between the entrances within the level
    for (IntVec2D pos1 : entrances) {
      Id<AbstractNode> abs1 = getAbsNodeId(level, pos1);
      for (IntVec2D pos2 : entrances) {
        if (pos1.equals(pos2)) {
          continue;
        }

        Id<AbstractNode> abs2 = getAbsNodeId(level, pos2);
        List<Tile> path = surface.findPath(new Tile(pos1), new Tile(pos2));
        addEdge(abs1, abs2, path.size() * Constants.COST_ONE);
      }
    }
  }

  public Id<AbstractNode> getAbsNodeId(int level, IntVec2D pos) {
    return positionToAbstractNodeIdMap.get(new Pair<>(level, pos));
  }

  public static void main(String[] args) throws Exception {
    Pair<Integer, Tile> from = new Pair<>(0, new Tile(new IntVec2D(1, 1)));
    Pair<Integer, Tile> to = new Pair<>(1, new Tile(new IntVec2D(2, 2)));

    HierarchicalGraph graph = new HierarchicalGraph();
    Id<AbstractNode> absFromId = graph.addAbstractNode(from.fst, from.snd.pos);
    Id<AbstractNode> absToId = graph.addAbstractNode(to.fst, to.snd.pos);
    graph.addEdge(absFromId, absToId, Constants.COST_ONE);
    Loggers.HPALogger.info("Search: %s -> %s, %s %s", absFromId, absFromId, from, to);
    AStar<AbstractNode> search = new AStar<AbstractNode>(graph, absFromId, absToId);
    Path<AbstractNode> abstractNodePath = search.findPath();
    System.out.println(abstractNodePath.pathNodes);
  }
}
