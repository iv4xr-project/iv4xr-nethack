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
import java.util.*;
import java.util.stream.Collectors;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.commons.lang.NotImplementedException;
import util.CustomVec2D;
import util.CustomVec3D;

public class HierarchicalNavigation implements Navigatable<CustomVec3D>, XPathfinder<CustomVec3D> {
  public final List<NetHackSurface> areas = new LinkedList<>();
  public final HierarchicalGraph hierarchicalGraph = new HierarchicalGraph();
  boolean perfect_memory_pathfinding = true;
  final HierarchicalMapFactory factory = new HierarchicalMapFactory();

  public HierarchicalNavigation(NetHackSurface surface) {
    areas.add(surface);
  }

  public void addNextArea(NetHackSurface area) {
    area.setPerfectMemoryPathfinding(perfect_memory_pathfinding);
    areas.add(area);
  }

  public Tile getTile(CustomVec3D loc) {
    return areas.get(loc.lvl).getTile(loc.pos);
  }

  public List<CustomVec3D> findPath(CustomVec3D from, CustomVec3D to) {
    if (from.lvl == to.lvl) {
      List<CustomVec2D> path = areas.get(from.lvl).findPath(from.pos, to.pos);
      if (path == null) {
        return null;
      }
      return path.stream().map(pos -> new CustomVec3D(from.lvl, pos)).collect(Collectors.toList());
    }

    Map<CustomVec2D, List<CustomVec2D>> pathsToArea1Stairs =
        pathsToStairs(from, hierarchicalGraph.levelToEntrancesMap.get(from.lvl));
    Map<CustomVec2D, List<CustomVec2D>> pathsToArea2Stairs =
        pathsToStairs(to, hierarchicalGraph.levelToEntrancesMap.get(to.lvl));

    for (CustomVec2D stairPosArea1 : pathsToArea1Stairs.keySet()) {
      Id<AbstractNode> stair1Id = hierarchicalGraph.getAbsNodeId(from.lvl, stairPosArea1);
      for (CustomVec2D stairPosArea2 : pathsToArea2Stairs.keySet()) {
        Id<AbstractNode> stair2Id = hierarchicalGraph.getAbsNodeId(to.lvl, stairPosArea2);
        AStar<AbstractNode> search = new AStar<>(hierarchicalGraph, stair1Id, stair2Id);
        Path<AbstractNode> path = search.findPath();
        if (path == null) {
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
            .map(pos -> new CustomVec3D(from.lvl, pos))
            .collect(Collectors.toList());
      }
    }

    // TODO: Implement shortest path method for multi-level
    return null;
  }

  private Map<CustomVec2D, List<CustomVec2D>> pathsToStairs(
      CustomVec3D from, List<CustomVec2D> stairPositions) {
    GridSurface surface = areas.get(from.lvl);
    Map<CustomVec2D, List<CustomVec2D>> paths = new HashMap<>();
    for (CustomVec2D targetPos : stairPositions) {
      List<CustomVec2D> path = surface.findPath(from.pos, targetPos);
      if (path == null) {
        continue;
      }

      paths.put(targetPos, path);
    }

    return paths;
  }

  public void markAsSeen(CustomVec3D loc) {
    areas.get(loc.lvl).markAsSeen(loc.pos);
  }

  public boolean hasbeenSeen(CustomVec3D loc) {
    return areas.get(loc.lvl).hasbeenSeen(loc.pos);
  }

  public List<CustomVec3D> getFrontier() {
    List<CustomVec3D> allFrontiers = new LinkedList<>();

    for (int i = 0; i < areas.size(); ++i) {
      NetHackSurface area = areas.get(i);
      int levelNr = i;
      List<CustomVec3D> frontiers =
          area.getFrontier().stream()
              .map(pos -> new CustomVec3D(levelNr, pos))
              .collect(Collectors.toList());
      allFrontiers.addAll(frontiers);
    }

    return allFrontiers;
  }

  public List<CustomVec3D> explore(CustomVec3D from, CustomVec3D to) {
    List<CustomVec3D> candidates = getFrontier();
    if (candidates.isEmpty()) {
      return null;
    }

    List<Pair<CustomVec3D, List<CustomVec3D>>> candidates2 =
        candidates.stream()
            .map(target -> new Pair<>(target, findPath(from, target)))
            .filter(d -> d.snd != null)
            .collect(Collectors.toList());
    if (candidates2.isEmpty()) {
      return null;
    } else {
      candidates2.sort(
          (d1, d2) -> {
            return Integer.compare(distanceCandidate(d1, to), distanceCandidate(d2, to));
          });
      return candidates2.get(0).snd;
    }
  }

  private int distanceCandidate(
      Pair<CustomVec3D, List<CustomVec3D>> candidate, CustomVec3D heuristicNode) {
    int c = candidate.fst.lvl;
    return c == heuristicNode.lvl
        ? candidate.snd.size()
        : Math.abs(c - heuristicNode.lvl) * 20000 + candidate.snd.size();
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
  public Iterable<CustomVec3D> neighbours(CustomVec3D loc) {
    //    return null;
    throw new NotImplementedException("Neighbours not implemented");
  }

  @Override
  public float heuristic(CustomVec3D from, CustomVec3D to) {
    //    return 0;
    throw new NotImplementedException("Heuristic between levels not valid");
  }

  @Override
  public float distance(CustomVec3D from, CustomVec3D to) {
    //    return 0;
    throw new NotImplementedException("Distance between levels not implemented");
  }
}
