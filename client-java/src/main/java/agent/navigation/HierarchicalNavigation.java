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
  boolean perfect_memory_pathfinding = true;
  final HierarchicalMapFactory factory = new HierarchicalMapFactory();
  public final HierarchicalGraph hierarchicalGraph = new HierarchicalGraph();

  public HierarchicalNavigation(NetHackSurface surface) {
    areas.add(surface);
  }

  public void addNextArea(NetHackSurface area) {
    area.setPerfectMemoryPathfinding(perfect_memory_pathfinding);
    areas.add(area);
  }

  public NetHackSurface level(Pair<Integer, Tile> level) {
    return areas.get(level.fst);
  }

  public List<Pair<Integer, Tile>> findPath(Pair<Integer, Tile> from, Pair<Integer, Tile> to) {
    int area1_id = (Integer) from.fst;
    int areaN_id = (Integer) to.fst;

    if (area1_id == areaN_id) {
      var path = getPathInLevel(area1_id, from.snd, to.snd);
      if (path.isEmpty()) {
        return null;
      }
      return path;
    }

    List<IntVec2D> fromStairs = hierarchicalGraph.levelToEntrancesMap.get(from.fst);
    List<IntVec2D> toStairs = hierarchicalGraph.levelToEntrancesMap.get(to.fst);
    for (IntVec2D fromStairPos : fromStairs) {
      Id<AbstractNode> fromId = hierarchicalGraph.getAbsNodeId(from.fst, fromStairPos);
      List<Pair<Integer, Tile>> fstPath =
          getPathInLevel(from.fst, from.snd, new Tile(fromStairPos));
      if (fstPath.isEmpty() && !from.snd.pos.equals(fromStairPos)) {
        continue;
      }
      for (IntVec2D toStairPos : toStairs) {
        Id<AbstractNode> toId = hierarchicalGraph.getAbsNodeId(to.fst, toStairPos);
        List<Pair<Integer, Tile>> sndPath = getPathInLevel(to.fst, to.snd, new Tile(toStairPos));
        if (sndPath.isEmpty() && to.snd.pos.equals(toStairPos)) {
          continue;
        }
        System.out.println("DID FIND PATH");
      }
    }

    throw new RuntimeException("Not yet implemented");
  }

  private List<Pair<Integer, Tile>> getPathInLevel(int level, Tile from, Tile to) {
    List<Tile> posPath = areas.get(level).findPath(from, to);
    return posPath.stream().map(tile -> new Pair<>(level, tile)).collect(Collectors.toList());
  }

  public void markAsSeen(Pair<Integer, Tile> ndx) {
    areas.get(ndx.fst).markAsSeen(ndx.snd);
  }

  public boolean hasbeenSeen(Pair<Integer, Tile> ndx) {
    return areas.get(ndx.fst).hasbeenSeen(ndx.snd);
  }

  public List<Pair<Integer, Tile>> getFrontier() {
    List<Pair<Integer, Tile>> allFrontiers = new LinkedList<>();

    for (int i = 0; i < areas.size(); ++i) {
      NetHackSurface area = areas.get(i);
      Integer levelNr = i;
      List<Pair<Integer, Tile>> frontiers =
          area.getFrontier().stream()
              .map(nd -> new Pair<>(levelNr, nd))
              .collect(Collectors.toList());
      allFrontiers.addAll(frontiers);
    }

    return allFrontiers;
  }

  public List<Pair<Integer, Tile>> explore(
      Pair<Integer, Tile> startNode, Pair<Integer, Tile> heuristicNode) {
    List<Pair<Integer, Tile>> candidates = getFrontier();
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
