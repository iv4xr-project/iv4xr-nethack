//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package agent.navigation;

import agent.navigation.hpastar.*;
import agent.navigation.hpastar.factories.HierarchicalMapFactory;
import agent.navigation.hpastar.graph.*;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.search.AStar;
import agent.navigation.hpastar.search.Path;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Tile;
import eu.iv4xr.framework.extensions.pathfinding.Navigatable;
import eu.iv4xr.framework.extensions.pathfinding.XPathfinder;
import java.util.*;
import java.util.stream.Collectors;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.commons.lang.NotImplementedException;
import util.CustomVec2D;
import util.CustomVec3D;
import util.Loggers;

public class HierarchicalNavigation implements Navigatable<CustomVec3D>, XPathfinder<CustomVec3D> {
  List<GridSurface> areas = new ArrayList<>();
  public final HierarchicalGraph hierarchicalGraph = new HierarchicalGraph();
  boolean perfect_memory_pathfinding = true;
  final HierarchicalMapFactory factory = new HierarchicalMapFactory();

  public HierarchicalNavigation(GridSurface surface) {
    areas.add(surface);
  }

  public void addNextArea(GridSurface area) {
    Loggers.AgentLogger.info("Adding a new level");
    area.setPerfectMemoryPathfinding(perfect_memory_pathfinding);
    areas.add(area);
  }

  public Tile getTile(CustomVec3D loc) {
    return areas.get(loc.lvl).getTile(loc.pos);
  }

  public List<CustomVec3D> findPath(CustomVec3D from, CustomVec3D to) {
    return findPaths(from, Collections.singletonList(to)).get(0);
  }

  public List<List<CustomVec3D>> findPaths(CustomVec3D from, List<CustomVec3D> targets) {
    assert !targets.isEmpty() : "Must contain at least one target";
    List<List<CustomVec3D>> paths = new ArrayList<>(targets.size());

    // Only paths to targets on same level, no
    if (targets.stream().allMatch(loc -> loc.lvl == from.lvl)) {
      for (CustomVec3D target : targets) {
        paths.add(findSameLvlPath(from, target));
      }
      return paths;
    }

    Map<CustomVec2D, List<CustomVec2D>> pathsToArea1Stairs =
        pathsToStairs(from, hierarchicalGraph.levelToEntrancesMap.get(from.lvl), false);

    for (CustomVec3D target : targets) {
      Map<CustomVec2D, List<CustomVec2D>> pathsFromStairs2Area =
          pathsToStairs(target, hierarchicalGraph.levelToEntrancesMap.get(target.lvl), true);

      Pair<CustomVec2D, CustomVec2D> pathPair = null;
      Integer shortestPathCost = null;

      for (CustomVec2D stairPosArea1 : pathsToArea1Stairs.keySet()) {
        Id<AbstractNode> stair1Id = hierarchicalGraph.getAbsNodeId(from.lvl, stairPosArea1);
        for (CustomVec2D stairPosArea2 : pathsFromStairs2Area.keySet()) {
          Id<AbstractNode> stair2Id = hierarchicalGraph.getAbsNodeId(target.lvl, stairPosArea2);
          AStar<AbstractNode> search = new AStar<>(hierarchicalGraph, stair1Id, stair2Id);
          Path<AbstractNode> path = search.findPath();
          if (path == null) {
            continue;
          }

          int costToStair1 = pathsToArea1Stairs.get(stairPosArea1).size() * Constants.COST_ONE;
          int costToStair2 = pathsFromStairs2Area.get(stairPosArea2).size() * Constants.COST_ONE;
          int pathCost = path.pathCost + costToStair1 + costToStair2;
          if (shortestPathCost == null || shortestPathCost > pathCost) {
            pathPair = new Pair<>(stairPosArea1, stairPosArea2);
            shortestPathCost = pathCost;
          }
        }
      }

      if (pathPair == null) {
        paths.add(null);
        continue;
      }

      List<CustomVec3D> path = NavUtils.addLevelNr(pathsToArea1Stairs.get(pathPair.fst), from.lvl);
      path.addAll(NavUtils.addLevelNr(pathsFromStairs2Area.get(pathPair.snd), target.lvl));

      System.out.printf(
          "Founds shortest path (Cost=%d) [%d]: %s%n", shortestPathCost, path.size(), path);
      paths.add(path);
    }

    return paths;
  }

  public List<CustomVec3D> findSameLvlPath(CustomVec3D from, CustomVec3D to) {
    assert from.lvl == to.lvl : "Must be on same level";
    List<CustomVec2D> path = areas.get(from.lvl).findPath(from.pos, to.pos);
    return NavUtils.addLevelNr(path, from.lvl);
  }

  private Map<CustomVec2D, List<CustomVec2D>> pathsToStairs(
      CustomVec3D from, List<CustomVec2D> stairPositions, boolean reverse) {
    Map<CustomVec2D, List<CustomVec2D>> paths = new HashMap<>();
    if (stairPositions == null) {
      return paths;
    }

    GridSurface surface = areas.get(from.lvl);
    for (CustomVec2D targetPos : stairPositions) {
      List<CustomVec2D> path = surface.findPath(from.pos, targetPos);
      if (path == null) {
        continue;
      }

      if (reverse) {
        Collections.reverse(path);
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
      List<CustomVec2D> frontiers = areas.get(i).getFrontier();
      allFrontiers.addAll(NavUtils.addLevelNr(frontiers, i));
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
    for (GridSurface area : areas) {
      area.wipeOutMemory();
    }
  }

  public boolean usingPerfectMemoryPathfinding() {
    return perfect_memory_pathfinding;
  }

  public void setPerfectMemoryPathfinding(Boolean flag) {
    perfect_memory_pathfinding = flag;
    for (GridSurface area : areas) {
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
