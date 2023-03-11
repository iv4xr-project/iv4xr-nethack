//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package agent.navigation;

import agent.navigation.hpastar.*;
import agent.navigation.hpastar.factories.HierarchicalMapFactory;
import agent.navigation.hpastar.graph.*;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.search.AStar;
import agent.navigation.hpastar.search.Path;
import agent.navigation.surface.Tile;
import eu.iv4xr.framework.extensions.pathfinding.Navigatable;
import eu.iv4xr.framework.extensions.pathfinding.XPathfinder;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.*;
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
    int area2_id = (Integer) to.fst;

    if (area1_id == area2_id) {
      var path = getPathInLevel(area1_id, from.snd, to.snd);
      if (path == null || path.isEmpty()) {
        return null;
      }
      return path;
    }

    Map<IntVec2D, List<Tile>> pathsToArea1Stairs =
        pathsToStairs(from.fst, from.snd.pos, hierarchicalGraph.levelToEntrancesMap.get(from.fst));
    Map<IntVec2D, List<Tile>> pathsToArea2Stairs =
        pathsToStairs(to.fst, to.snd.pos, hierarchicalGraph.levelToEntrancesMap.get(to.fst));

    for (IntVec2D stairPosArea1 : pathsToArea1Stairs.keySet()) {
      Id<AbstractNode> stair1Id = hierarchicalGraph.getAbsNodeId(from.fst, stairPosArea1);
      for (IntVec2D stairPosArea2 : pathsToArea2Stairs.keySet()) {
        Id<AbstractNode> stair2Id = hierarchicalGraph.getAbsNodeId(to.fst, stairPosArea2);
        AStar<AbstractNode> search = new AStar<>(hierarchicalGraph, stair1Id, stair2Id);
        Path<AbstractNode> path = search.findPath();
        if (path.pathNodes.isEmpty()) {
          continue;
        }
        //        System.out.printf(
        //            "Found path: %s -> %s. PathToStair1 (%s): %s PathFromStair2(%s): %s%n",
        //            from,
        //            to,
        //            stair1Id,
        //            pathsToArea1Stairs.get(stairPosArea1),
        //            stair2Id,
        //            pathsToArea2Stairs.get(stairPosArea2));
        //        int costToStair1 = pathsToArea1Stairs.get(stairPosArea1).size() *
        // Constants.COST_ONE;
        //        int costToStair2 = pathsToArea2Stairs.get(stairPosArea2).size() *
        // Constants.COST_ONE;
        //        int costBetweenStairs = path.pathCost;
        //        System.out.printf(
        //            "Cost of path %d: %d > %d < %d%n",
        //            costBetweenStairs + costToStair1 + costToStair2,
        //            costToStair1,
        //            costBetweenStairs,
        //            costToStair2);
        return pathsToArea1Stairs.get(stairPosArea1).stream()
            .map(pos -> new Pair<>(from.fst, pos))
            .collect(Collectors.toList());
      }
    }

    // TODO: Implement shortest path method for multi-level
    return null;
  }

  private Map<IntVec2D, List<Tile>> pathsToStairs(
      int level, IntVec2D startPos, List<IntVec2D> stairPositions) {
    GridSurface surface = areas.get(level);
    Tile startTile = new Tile(startPos);
    Map<IntVec2D, List<Tile>> paths = new HashMap<>();
    for (IntVec2D targetPos : stairPositions) {
      if (startPos.equals(targetPos)) {
        paths.put(targetPos, new ArrayList<>());
      } else {
        List<Tile> tilePath = surface.findPath(startTile, new Tile(targetPos));
        if (tilePath == null || tilePath.isEmpty()) {
          continue;
        }

        paths.put(targetPos, tilePath);
      }
    }

    return paths;
  }

  private List<Pair<Integer, Tile>> getPathInLevel(int level, Tile from, Tile to) {
    List<Tile> posPath = areas.get(level).findPath(from, to);
    if (posPath == null) {
      return null;
    }
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
