//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package agent.navigation;

import agent.navigation.hpastar.*;
import agent.navigation.hpastar.factories.NodeBackup;
import agent.navigation.hpastar.graph.AbstractEdge;
import agent.navigation.hpastar.graph.AbstractNode;
import agent.navigation.hpastar.graph.AbstractNodeInfo;
import agent.navigation.hpastar.graph.ConcreteNode;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.search.HierarchicalSearch;
import agent.navigation.hpastar.smoother.SmoothWizard;
import agent.navigation.surface.Tile;
import eu.iv4xr.framework.extensions.pathfinding.CanDealWithDynamicObstacle;
import eu.iv4xr.framework.extensions.pathfinding.XPathfinder;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.*;
import java.util.stream.Collectors;
import nl.uu.cs.aplib.utils.Pair;

public class HierarchicalAreasNavigation<
        NodeId, Nav extends IPassability & XPathfinder<NodeId> & CanDealWithDynamicObstacle<NodeId>>
    implements IGraphCreator<Pair<Integer, NodeId>>,
        XPathfinder<Pair<Integer, NodeId>>,
        CanDealWithDynamicObstacle<Pair<Integer, NodeId>> {
  public List<Nav> areas = new LinkedList<>();
  boolean perfect_memory_pathfinding = false;
  Map<Id<AbstractNode>, NodeBackup> nodeBackups = new HashMap<>();
  HierarchicalMap map;

  public HierarchicalAreasNavigation(Nav nav) {
    areas.add(nav);
    map = new NetHackMapFactory().createHierarchicalMap(nav.getConcreteMap());
  }

  public void addNextArea(Nav area) {
    areas.add(area);
    if (areas.size() != 1) {
      area.setPerfectMemoryPathfinding(this.perfect_memory_pathfinding);
    }
  }

  public List<Pair<Integer, NodeId>> findPath(
      Pair<Integer, NodeId> from, Pair<Integer, NodeId> to) {
    int area1_id = (Integer) from.fst;
    int areaN_id = (Integer) to.fst;

    Id<AbstractNode> startAbsNode = insertAbstractNode(((Tile) from.snd).pos);
    Id<AbstractNode> targetAbsNode = insertAbstractNode(((Tile) to.snd).pos);
    assert !startAbsNode.equals(targetAbsNode);
    int maxPathsToRefine = Integer.MAX_VALUE;
    HierarchicalSearch hierarchicalSearch = new HierarchicalSearch();
    List<AbstractPathNode> abstractPath =
        hierarchicalSearch.doHierarchicalSearch(
            map, startAbsNode, targetAbsNode, 10, maxPathsToRefine);
    List<IPathNode> path =
        hierarchicalSearch.abstractPathToLowLevelPath(
            map, abstractPath, map.width, maxPathsToRefine);

    SmoothWizard smoother = new SmoothWizard(areas.get(0).getConcreteMap(), path);
    path = smoother.smoothPath();
    removeAbstractNode(targetAbsNode);
    removeAbstractNode(startAbsNode);
    List<IntVec2D> posPath = toPositionPath(path, areas.get(0).getConcreteMap());
    return posPath.stream()
        .map(pos -> new Pair<>(0, (NodeId) new Tile(pos)))
        .collect(Collectors.toList());
  }

  private Id<AbstractNode> insertAbstractNode(IntVec2D pos) {
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

  public void removeAbstractNode(Id<AbstractNode> nodeId) {
    if (nodeBackups.containsKey(nodeId)) {
      restoreNodeBackup(nodeId);
    } else {
      map.removeAbstractNode(nodeId);
    }
  }

  private void restoreNodeBackup(Id<AbstractNode> nodeId) {
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

  private List<IntVec2D> toPositionPath(List<IPathNode> path, ConcreteMap concreteMap) {
    return path.stream()
        .map(
            (p) -> {
              if (p instanceof ConcretePathNode) {
                ConcretePathNode concretePathNode = (ConcretePathNode) p;
                return concreteMap.graph.getNodeInfo(concretePathNode.id).position;
              }

              AbstractPathNode abstractPathNode = (AbstractPathNode) p;
              return map.abstractGraph.getNodeInfo(abstractPathNode.id).position;
            })
        .collect(Collectors.toList());
  }

  public void addObstacle(Pair<Integer, NodeId> o) {
    ((CanDealWithDynamicObstacle) ((XPathfinder) this.areas.get((Integer) o.fst)))
        .addObstacle(o.snd);
  }

  public void removeObstacle(Pair<Integer, NodeId> o) {
    ((CanDealWithDynamicObstacle) ((XPathfinder) this.areas.get((Integer) o.fst)))
        .removeObstacle(o.snd);
  }

  public void setBlockingState(Pair<Integer, NodeId> o, boolean isBlocking) {
    int area = (Integer) o.fst;
    NodeId nd = o.snd;
    ((CanDealWithDynamicObstacle) ((XPathfinder) this.areas.get(area)))
        .setBlockingState(nd, isBlocking);
  }

  public void markAsSeen(Pair<Integer, NodeId> ndx) {
    ((XPathfinder) this.areas.get((Integer) ndx.fst)).markAsSeen(ndx.snd);
  }

  public boolean hasbeenSeen(Pair<Integer, NodeId> ndx) {
    return ((XPathfinder) this.areas.get((Integer) ndx.fst)).hasbeenSeen(ndx.snd);
  }

  public List<Pair<Integer, NodeId>> getFrontier() {
    List<Pair<Integer, NodeId>> frontiers = new LinkedList();

    for (int a = 0; a < this.areas.size(); ++a) {
      Nav nav = this.areas.get(a);
      Integer a_ = a;
      List<Pair<Integer, NodeId>> fr =
          (List)
              nav.getFrontier().stream()
                  .map(
                      (nd) -> {
                        return new Pair(a_, nd);
                      })
                  .collect(Collectors.toList());
      frontiers.addAll(fr);
    }

    return frontiers;
  }

  public List<Pair<Integer, NodeId>> explore(
      Pair<Integer, NodeId> startNode, Pair<Integer, NodeId> heuristicNode) {
    List<Pair<Integer, NodeId>> candidates = this.getFrontier();
    if (candidates.size() == 0) {
      return null;
    } else {
      List<Pair<Pair<Integer, NodeId>, List<Pair<Integer, NodeId>>>> candidates2 =
          (List)
              candidates.stream()
                  .map(
                      (c) -> {
                        return new Pair(c, this.findPath(startNode, c));
                      })
                  .filter(
                      (d) -> {
                        return d.snd != null;
                      })
                  .collect(Collectors.toList());
      if (candidates2.size() == 0) {
        return null;
      } else {
        candidates2.sort(
            (d1, d2) -> {
              return Integer.compare(
                  this.distanceCandidate(d1, heuristicNode),
                  this.distanceCandidate(d2, heuristicNode));
            });
        return (List) ((Pair) candidates2.get(0)).snd;
      }
    }
  }

  private int distanceCandidate(
      Pair<Pair<Integer, NodeId>, List<Pair<Integer, NodeId>>> candidate,
      Pair<Integer, NodeId> heuristicNode) {
    int c = (Integer) ((Pair) candidate.fst).fst;
    return c == (Integer) heuristicNode.fst
        ? ((List) candidate.snd).size()
        : Math.abs(c - (Integer) heuristicNode.fst) * 20000 + ((List) candidate.snd).size();
  }

  public void wipeOutMemory() {
    Iterator var1 = this.areas.iterator();

    while (var1.hasNext()) {
      Nav nav = (Nav) var1.next();
      nav.wipeOutMemory();
    }
  }

  public boolean usingPerfectMemoryPathfinding() {
    return this.perfect_memory_pathfinding;
  }

  public void setPerfectMemoryPathfinding(Boolean flag) {
    this.perfect_memory_pathfinding = flag;
    Iterator var2 = this.areas.iterator();

    while (var2.hasNext()) {
      Nav nav = (Nav) var2.next();
      nav.setPerfectMemoryPathfinding(flag);
    }
  }

  public boolean isBlocking(Pair<Integer, NodeId> o) {
    Nav nav = this.areas.get((Integer) o.fst);
    return ((CanDealWithDynamicObstacle) nav).isBlocking(o.snd);
  }

  public String toString() {
    StringBuffer z = new StringBuffer();

    for (int a = 0; a < this.areas.size(); ++a) {
      if (a > 0) {
        z.append("\n");
      }

      Nav nav = this.areas.get(a);
      z.append("=== Area " + a);
      z.append("\n" + String.valueOf(nav));
    }

    return z.toString();
  }

  @Override
  public void addEdge(
      Pair<Integer, Pair<Integer, NodeId>> from, Pair<Integer, Pair<Integer, NodeId>> to) {}

  @Override
  public void addNode(Pair<Integer, Pair<Integer, NodeId>> node) {}

  @Override
  public void removeNode(Pair<Integer, Pair<Integer, NodeId>> node) {}

  @Override
  public void removeEdge(
      Pair<Integer, Pair<Integer, NodeId>> from, Pair<Integer, Pair<Integer, NodeId>> to) {}
}
