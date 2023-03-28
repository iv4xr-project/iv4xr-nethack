package agent.navigation.hpastar;

import agent.navigation.GridSurface;
import agent.navigation.hpastar.graph.*;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.infrastructure.IMap;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.search.AStar;
import agent.navigation.hpastar.search.IdPath;
import agent.navigation.hpastar.search.Path;
import java.util.*;
import util.CustomVec2D;
import util.CustomVec3D;
import util.Loggers;

public class HierarchicalGraph implements IMap<AbstractNode> {
  public final AbstractGraph abstractGraph = new AbstractGraph();
  public final Map<CustomVec3D, Id<AbstractNode>> positionToAbstractNodeIdMap = new HashMap<>();
  public final Map<Integer, List<CustomVec2D>> levelToEntrancesMap = new HashMap<>();

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

  public Id<AbstractNode> addAbstractNode(CustomVec3D loc) {
    Id<AbstractNode> nodeId = getAbsNodeId(loc);
    if (nodeId != null) {
      System.out.printf("HierarchGraph Ignored AddNode %s: %s%n", nodeId, loc);
      return nodeId;
    }

    if (!levelToEntrancesMap.containsKey(loc.lvl)) {
      levelToEntrancesMap.put(loc.lvl, new ArrayList<>());
    }
    levelToEntrancesMap.get(loc.lvl).add(loc.pos);

    System.out.printf("HierarchGraph AddNode %s%n", loc);
    Id<AbstractNode> absNodeId = new Id<AbstractNode>().from(abstractGraph.nextId);
    AbstractNodeInfo nodeInfo = new AbstractNodeInfo(absNodeId, loc.lvl, null, loc.pos, null);
    abstractGraph.addNode(nodeInfo.id, nodeInfo);
    positionToAbstractNodeIdMap.put(loc, absNodeId);
    return absNodeId;
  }

  public void removeAbstractNode(Id<AbstractNode> abstractNodeId) {
    AbstractNodeInfo abstractNodeInfo = abstractGraph.getNodeInfo(abstractNodeId);
    CustomVec3D pos = new CustomVec3D(abstractNodeInfo.level, abstractNodeInfo.position);
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
    IdPath<AbstractNode> idPath = search.findPath();
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

  public void createEdgesWithinLevel(int level, GridSurface surface) {
    List<CustomVec2D> entrances = levelToEntrancesMap.get(level);
    if (entrances == null) {
      return;
    }

    // Remove edges between entrances within the same level
    for (CustomVec2D pos : entrances) {
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
    for (CustomVec2D pos1 : entrances) {
      Id<AbstractNode> abs1 = getAbsNodeId(level, pos1);
      for (CustomVec2D pos2 : entrances) {
        if (pos1.equals(pos2)) {
          continue;
        }

        Id<AbstractNode> abs2 = getAbsNodeId(level, pos2);
        Path<CustomVec2D> path = surface.findPath(pos1, pos2);
        addEdge(abs1, abs2, path.cost);
      }
    }
  }

  public Id<AbstractNode> getAbsNodeId(CustomVec3D loc) {
    return positionToAbstractNodeIdMap.get(loc);
  }

  public Id<AbstractNode> getAbsNodeId(int lvl, CustomVec2D pos) {
    return getAbsNodeId(new CustomVec3D(lvl, pos));
  }

  public static void main(String[] args) throws Exception {
    CustomVec3D from = new CustomVec3D(0, new CustomVec2D(1, 1));
    CustomVec3D to = new CustomVec3D(1, new CustomVec2D(2, 2));

    HierarchicalGraph graph = new HierarchicalGraph();
    Id<AbstractNode> absFromId = graph.addAbstractNode(from);
    Id<AbstractNode> absToId = graph.addAbstractNode(to);
    graph.addEdge(absFromId, absToId, Constants.COST_ONE);
    Loggers.HPALogger.info("Search: %s -> %s, %s %s", absFromId, absFromId, from, to);
    AStar<AbstractNode> search = new AStar<>(graph, absFromId, absToId);
    IdPath<AbstractNode> abstractNodeIdPath = search.findPath();
    System.out.println(abstractNodeIdPath.pathNodes);
  }
}
