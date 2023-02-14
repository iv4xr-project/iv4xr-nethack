//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package agent.navigation;

import agent.navigation.hpastar.*;
import agent.navigation.hpastar.factories.HierarchicalMapFactory;
import agent.navigation.hpastar.graph.*;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.surface.Tile;
import eu.iv4xr.framework.extensions.pathfinding.Navigatable;
import eu.iv4xr.framework.extensions.pathfinding.XPathfinder;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import nl.uu.cs.aplib.utils.Pair;

public class HierarchicalNavigation
    implements Navigatable<Pair<Integer, Tile>>, XPathfinder<Pair<Integer, Tile>> {
  public final List<NetHackSurface> areas = new LinkedList<>();
  boolean perfect_memory_pathfinding = false;
  final HierarchicalMapFactory factory;

  public HierarchicalNavigation(NetHackSurface surface) {
    areas.add(surface);
    factory = new HierarchicalMapFactory();
  }

  public void addNextArea(NetHackSurface area) {
    areas.add(area);
    if (areas.size() != 1) {
      area.setPerfectMemoryPathfinding(perfect_memory_pathfinding);
    }
  }

  public NetHackSurface level(Pair<Integer, Tile> level) {
    return areas.get(level.fst);
  }

  public List<Pair<Integer, Tile>> findPath(Pair<Integer, Tile> from, Pair<Integer, Tile> to) {
    int area1_id = (Integer) from.fst;
    int areaN_id = (Integer) to.fst;

    assert area1_id == areaN_id : "No navigation between levels as of now";
    var posPath = areas.get(area1_id).findPath(from.snd, to.snd);
    if (posPath.isEmpty()) {
      return null;
    }
    return posPath.stream().map(tile -> new Pair<>(0, tile)).collect(Collectors.toList());
  }

  private HierarchicalMap map() {
    return areas.get(0).hierarchicalMap;
  }

  private ConcreteMap concreteMap() {
    return factory.concreteMap;
  }

  private Id<AbstractNode> insertAbstractNode(IntVec2D pos) {
    Id<ConcreteNode> nodeId = new Id<ConcreteNode>().from(pos.y * map().size.width + pos.x);
    Id<AbstractNode> abstractNodeId = factory.insertNodeIntoHierarchicalMap(map(), nodeId, pos);
    map().addHierarchicalEdgesForAbstractNode(abstractNodeId);
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
              return map().abstractGraph.getNodeInfo(abstractPathNode.id).position;
            })
        .collect(Collectors.toList());
  }

  public void markAsSeen(Pair<Integer, Tile> ndx) {
    areas.get((Integer) ndx.fst).markAsSeen(ndx.snd);
  }

  public boolean hasbeenSeen(Pair<Integer, Tile> ndx) {
    return areas.get((Integer) ndx.fst).hasbeenSeen(ndx.snd);
  }

  public List<Pair<Integer, Tile>> getFrontier() {
    List<Pair<Integer, Tile>> frontiers = new LinkedList<>();

    for (int a = 0; a < areas.size(); ++a) {
      NetHackSurface area = areas.get(a);
      Integer a_ = a;
      List<Pair<Integer, Tile>> fr =
          area.getFrontier().stream().map(nd -> new Pair<>(a_, nd)).collect(Collectors.toList());
      frontiers.addAll(fr);
    }

    return frontiers;
  }

  public List<Pair<Integer, Tile>> explore(
      Pair<Integer, Tile> startNode, Pair<Integer, Tile> heuristicNode) {
    List<Pair<Integer, Tile>> candidates = this.getFrontier();
    if (candidates.isEmpty()) {
      return null;
    } else {
      List<Pair<Pair<Integer, Tile>, List<Pair<Integer, Tile>>>> candidates2 =
          candidates.stream()
              .map(c -> new Pair<>(c, this.findPath(startNode, c)))
              .filter(d -> d.snd != null)
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
        return candidates2.get(0).snd;
      }
    }
  }

  private int distanceCandidate(
      Pair<Pair<Integer, Tile>, List<Pair<Integer, Tile>>> candidate,
      Pair<Integer, Tile> heuristicNode) {
    int c = candidate.fst.fst;
    return c == heuristicNode.fst
        ? candidate.snd.size()
        : Math.abs(c - heuristicNode.fst) * 20000 + candidate.snd.size();
  }

  public void wipeOutMemory() {
    for (NetHackSurface area : areas) {
      area.wipeOutMemory();
    }
  }

  public boolean usingPerfectMemoryPathfinding() {
    return perfect_memory_pathfinding;
  }

  public void setPerfectMemoryPathfinding(Boolean flag) {
    perfect_memory_pathfinding = flag;
    for (NetHackSurface area : areas) {
      area.setPerfectMemoryPathfinding(flag);
    }
  }

  //  public boolean isBlocking(Pair<Integer, Tile> o) {
  //    NetHackSurface area = areas.get(o.fst);
  //    return area.isBlocking(o.snd);
  //  }

  @Override
  public Iterable<Pair<Integer, Tile>> neighbours(Pair<Integer, Tile> integerTilePair) {
    return null;
  }

  @Override
  public float heuristic(Pair<Integer, Tile> integerTilePair, Pair<Integer, Tile> nodeId1) {
    return 0;
  }

  @Override
  public float distance(Pair<Integer, Tile> integerTilePair, Pair<Integer, Tile> nodeId1) {
    return 0;
  }
}
