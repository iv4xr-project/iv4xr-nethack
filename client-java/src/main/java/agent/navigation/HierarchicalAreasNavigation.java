//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package agent.navigation;

import agent.navigation.hpastar.*;
import agent.navigation.hpastar.factories.EntranceStyle;
import agent.navigation.hpastar.factories.GraphFactory;
import agent.navigation.hpastar.factories.HierarchicalMapFactory;
import agent.navigation.hpastar.factories.NodeBackup;
import agent.navigation.hpastar.graph.AbstractNode;
import agent.navigation.hpastar.graph.ConcreteEdgeInfo;
import agent.navigation.hpastar.graph.ConcreteNode;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.search.HierarchicalSearch;
import agent.navigation.hpastar.smoother.SmoothWizard;
import agent.navigation.hpastar.utils.RefSupport;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.StraightWalkable;
import agent.navigation.surface.Tile;
import eu.iv4xr.framework.extensions.pathfinding.CanDealWithDynamicObstacle;
import eu.iv4xr.framework.extensions.pathfinding.XPathfinder;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import nethack.object.Level;
import nl.uu.cs.aplib.utils.Pair;

public class HierarchicalAreasNavigation<
        NodeId, Nav extends IPassability & XPathfinder<NodeId> & CanDealWithDynamicObstacle<NodeId>>
    implements IGraphCreator<NodeId>,
        XPathfinder<Pair<Integer, NodeId>>,
        CanDealWithDynamicObstacle<Pair<Integer, NodeId>> {
  public List<Nav> areas = new LinkedList<>();
  boolean perfect_memory_pathfinding = false;
  Map<Id<AbstractNode>, NodeBackup> nodeBackups = new HashMap<>();
  HierarchicalMap map;
  ConcreteMap concreteMap;
  HierarchicalMapFactory mapFactory;

  public HierarchicalAreasNavigation(Nav nav) {
    areas.add(nav);
    concreteMap = areas.get(0).getConcreteMap();
    mapFactory = new HierarchicalMapFactory();
    map =
        mapFactory.createHierarchicalMap(concreteMap, 8, 10, EntranceStyle.EndEntrance, Level.SIZE);
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
            map, abstractPath, map.size.width, maxPathsToRefine);

    SmoothWizard smoother = new SmoothWizard(concreteMap, path);
    path = smoother.smoothPath();
    mapFactory.removeAbstractNode(map, targetAbsNode);
    mapFactory.removeAbstractNode(map, startAbsNode);
    List<IntVec2D> posPath = toPositionPath(path, concreteMap);
    return posPath.stream()
        .map(pos -> new Pair<>(0, (NodeId) new Tile(pos)))
        .collect(Collectors.toList());
  }

  private Id<AbstractNode> insertAbstractNode(IntVec2D pos) {
    Id<ConcreteNode> nodeId = new Id<ConcreteNode>().from(pos.y * map.size.width + pos.x);
    Id<AbstractNode> abstractNodeId = mapFactory.insertNodeIntoHierarchicalMap(map, nodeId, pos);
    map.addHierarchicalEdgesForAbstractNode(abstractNodeId);
    return abstractNodeId;
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
    areas.get(o.fst).addObstacle(o.snd);
    Tile t = (Tile) o.snd;
    if (t instanceof StraightWalkable) {
      addEdges(o);
    } else {
      removeEdges(o);
    }
  }

  public void removeObstacle(Pair<Integer, NodeId> o) {
    areas.get((Integer) o.fst).removeObstacle(o.snd);
    addEdges(o);
  }

  public void setBlockingState(Pair<Integer, NodeId> o, boolean isBlocking) {
    int area = (Integer) o.fst;
    NodeId nd = o.snd;
    areas.get(area).setBlockingState(nd, isBlocking);
  }

  public void markAsSeen(Pair<Integer, NodeId> ndx) {
    areas.get((Integer) ndx.fst).markAsSeen(ndx.snd);
  }

  public boolean hasbeenSeen(Pair<Integer, NodeId> ndx) {
    return areas.get((Integer) ndx.fst).hasbeenSeen(ndx.snd);
  }

  public List<Pair<Integer, NodeId>> getFrontier() {
    List<Pair<Integer, NodeId>> frontiers = new LinkedList<>();

    for (int a = 0; a < this.areas.size(); ++a) {
      Nav nav = this.areas.get(a);
      Integer a_ = a;
      List<Pair<Integer, NodeId>> fr =
          (List)
              nav.getFrontier().stream().map((nd) -> new Pair(a_, nd)).collect(Collectors.toList());
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
                  .map((c) -> new Pair(c, this.findPath(startNode, c)))
                  .filter((d) -> d.snd != null)
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
    for (Nav nav : this.areas) {
      nav.wipeOutMemory();
    }
  }

  public boolean usingPerfectMemoryPathfinding() {
    return this.perfect_memory_pathfinding;
  }

  public void setPerfectMemoryPathfinding(Boolean flag) {
    perfect_memory_pathfinding = flag;
    for (Nav nav : this.areas) {
      nav.setPerfectMemoryPathfinding(flag);
    }
  }

  public boolean isBlocking(Pair<Integer, NodeId> o) {
    Nav nav = this.areas.get((Integer) o.fst);
    return nav.isBlocking(o.snd);
  }

  public String toString() {
    StringBuffer z = new StringBuffer();

    for (int a = 0; a < this.areas.size(); ++a) {
      if (a > 0) {
        z.append("\n");
      }

      Nav nav = this.areas.get(a);
      z.append("=== Area ").append(a);
      z.append("\n").append(String.valueOf(nav));
    }

    return z.toString();
  }

  @Override
  public void addEdges(Pair<Integer, NodeId> o) {
    Tile t = (Tile) o.snd;
    Cluster originalCluster = map.findClusterForPosition(t.pos);
    ConcreteMap concreteMap = originalCluster.subConcreteMap;
    var nodeId =
        GraphFactory.getNodeByPos(
                concreteMap.graph,
                t.pos.x % map.clusterSize,
                t.pos.y % map.clusterSize,
                map.clusterSize)
            .nodeId;
    var node = concreteMap.graph.getNode(nodeId);
    node.info.isObstacle = false;

    List<IntVec2D> neighbours = NavUtils.neighbourCoordinates(t.pos, map.size, true);
    for (IntVec2D neighbourPos : neighbours) {
      if (!concreteMap.passability.canEnter(neighbourPos, new RefSupport<>())) {
        continue;
      }
      Cluster neighbourCluster = map.findClusterForPosition(neighbourPos);
      ConcreteMap neighbourConcreteMap = neighbourCluster.subConcreteMap;
      var neighBourId =
          GraphFactory.getNodeByPos(
                  neighbourConcreteMap.graph,
                  neighbourPos.x % map.clusterSize,
                  neighbourPos.y % map.clusterSize,
                  map.clusterSize)
              .nodeId;
      concreteMap.graph.addEdge(nodeId, neighBourId, new ConcreteEdgeInfo(Constants.COST_ONE));
    }
  }

  @Override
  public void removeEdges(Pair<Integer, NodeId> o) {
    Tile t = (Tile) o.snd;
    assert !(t instanceof StraightWalkable);

    Cluster cluster = map.findClusterForPosition(t.pos);
    ConcreteMap concreteMap = cluster.subConcreteMap;
    var nodeId =
        GraphFactory.getNodeByPos(
                concreteMap.graph,
                t.pos.x % map.clusterSize,
                t.pos.y % map.clusterSize,
                map.clusterSize)
            .nodeId;
    var node = concreteMap.graph.getNode(nodeId);
    node.info.isObstacle = true;

    concreteMap.graph.removeEdgesFromAndToNode(nodeId);
  }

  @Override
  public void addNode(Pair<Integer, NodeId> node) {}

  @Override
  public void removeNode(Pair<Integer, NodeId> node) {}
}
