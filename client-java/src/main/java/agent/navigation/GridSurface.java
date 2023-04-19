package agent.navigation;

import agent.navigation.hpastar.*;
import agent.navigation.hpastar.factories.EntranceStyle;
import agent.navigation.hpastar.factories.HierarchicalMapFactory;
import agent.navigation.hpastar.graph.*;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.passabilities.EmptyPassability;
import agent.navigation.hpastar.search.HierarchicalSearch;
import agent.navigation.hpastar.search.Path;
import agent.navigation.hpastar.smoother.Direction;
import agent.navigation.hpastar.utils.RefSupport;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.*;
import eu.iv4xr.framework.extensions.pathfinding.Navigatable;
import eu.iv4xr.framework.extensions.pathfinding.XPathfinder;
import java.util.*;
import java.util.stream.Collectors;
import nethack.enums.Color;
import nethack.world.tiles.Door;
import nethack.world.tiles.Viewable;
import util.ColoredStringBuilder;
import util.CustomVec2D;

/**
 * Representing a navigation graph over a 2D tiled-world. The world is assumed to be made of
 * tiles/squares, arranged from tile (0,0) to tile (maxX-1,maxY-1) to form a rectangle world.
 *
 * <p>The tiles are not explicitly stored. Rather, we only store non-navigable tiles. These are
 * tiles that block movement through them. There are two types: Wall and Door. A wall is always
 * non-navigable. A door can be made blocking/unblocking.
 *
 * <p>The class also implements {@link Navigatable} and {@link XPathfinder}, so it offers methods to
 * do pathfinding and exploration over the world.
 *
 * @author Wish
 */
public class GridSurface implements Navigatable<CustomVec2D> {
  public final Tile[][] tiles;
  public final Map<String, HashSet<CustomVec2D>> tileTypes = new HashMap<>();
  public final Set<CustomVec2D> frontiers = new HashSet<>();
  public final HierarchicalMap hierarchicalMap;

  /**
   * If true, the pathfinder will assume that the whole NavGraph has been "seen", so no vertex would
   * count as unreachable because it is still unseen. This essentially turns off memory-based path
   * finding. The default of this flag is false.
   */
  private boolean perfect_memory_pathfinding = true;

  public final ConcreteMap concreteMap;

  public GridSurface(Size size, int clusterSize) {
    tiles = new Tile[size.height][size.width];
    concreteMap = new EmptyPassability(size).getConcreteMap();
    hierarchicalMap =
        new HierarchicalMapFactory()
            .createHierarchicalMap(concreteMap, clusterSize, 1, EntranceStyle.EndEntrance, size);
  }

  public void updateTiles(List<Tile> newTiles) {
    Map<Id<Cluster>, Set<Direction>> entrances = new HashMap<>();
    Set<Id<Cluster>> updatedClusters = new HashSet<>();
    for (Tile tile : newTiles) {
      boolean updated = updateTile(tile);
      if (!updated) {
        continue;
      }

      Id<Cluster> clusterId = hierarchicalMap.findClusterForPosition(tile.pos).id;
      updatedClusters.add(clusterId);

      boolean blocked = !(tile instanceof Walkable);
      if (blocked) {
        GridSurfaceFactory.removeEdges(this, tile);
        continue;
      }

      Set<Direction> directions = GridSurfaceFactory.addEdges(this, tile);
      addClusterEdges(entrances, tile, directions);
    }

    createEntrances(entrances);

    // Update clusters after updating tiles
    for (Id<Cluster> clusterId : updatedClusters) {
      resetIntraClusterPaths(clusterId);
    }
  }

  private void resetIntraClusterPaths(Id<Cluster> clusterId) {
    Cluster cluster = hierarchicalMap.getCluster(clusterId);
    cluster.resetComputedPaths();

    List<Id<AbstractNode>> nodeIds =
        cluster.entrancePoints.stream()
            .map(entrancePoint -> entrancePoint.abstractNodeId)
            .collect(Collectors.toList());
    for (EntrancePoint entrance : cluster.entrancePoints) {
      Id<AbstractNode> abstractNodeId = entrance.abstractNodeId;
      AbstractNode abstractNode = hierarchicalMap.abstractGraph.getNode(abstractNodeId);
      for (Id<AbstractNode> nodeId : nodeIds) {
        abstractNode.removeEdge(nodeId);
      }
    }
    cluster.createIntraClusterEdges();
    GridSurfaceFactory.createIntraClusterEdges(this, cluster);
  }

  private void addClusterEdges(
      Map<Id<Cluster>, Set<Direction>> entrances, Tile tile, Set<Direction> directions) {
    if (directions.isEmpty()) {
      return;
    }

    Cluster cluster = hierarchicalMap.findClusterForPosition(tile.pos);
    for (Direction direction : directions) {
      Cluster mapCluster;
      Direction mapDirection;

      if (direction == Direction.South || direction == Direction.East) {
        mapCluster = GridSurfaceFactory.getClusterInDirection(this, cluster, direction);
        if (direction == Direction.South) {
          mapDirection = Direction.North;
        } else {
          mapDirection = Direction.West;
        }
      } else {
        mapCluster = cluster;
        mapDirection = direction;
      }
      Id<Cluster> mapClusterId = mapCluster.id;
      if (!entrances.containsKey(mapClusterId)) {
        entrances.put(mapClusterId, new HashSet<>());
      }
      entrances.get(mapClusterId).add(mapDirection);
    }
  }

  private void createEntrances(Map<Id<Cluster>, Set<Direction>> entrances) {
    Set<Id<Cluster>> updateIntraClusterEdges = new HashSet<>();

    for (Id<Cluster> clusterId : entrances.keySet()) {
      Cluster cluster = hierarchicalMap.getCluster(clusterId);
      updateIntraClusterEdges.add(clusterId);
      int top = cluster.origin.y;
      int left = cluster.origin.x;
      for (Direction direction : entrances.get(clusterId)) {
        Cluster neighbourCluster =
            GridSurfaceFactory.getClusterInDirection(this, cluster, direction);
        updateIntraClusterEdges.add(neighbourCluster.id);
        if (direction == Direction.North) {
          GridSurfaceFactory.createEntrancesOnTop(
              left,
              left + cluster.size.width - 1,
              top - 1,
              neighbourCluster,
              cluster,
              new RefSupport<>(0));
        } else if (direction == Direction.West) {
          GridSurfaceFactory.createEntrancesOnLeft(
              top,
              top + cluster.size.height - 1,
              left - 1,
              neighbourCluster,
              cluster,
              new RefSupport<>(0));
        }
      }
    }

    // Update intra-cluster edges
    for (Id<Cluster> clusterId : updateIntraClusterEdges) {
      Cluster cluster = hierarchicalMap.getCluster(clusterId);
      cluster.createIntraClusterEdges();
      GridSurfaceFactory.createIntraClusterEdges(this, cluster);
    }
  }

  private boolean updateTile(Tile tile) {
    updatePassibility(tile);
    Tile prevTile = tiles[tile.pos.y][tile.pos.x];
    if (prevTile == null) {
      replaceTile(prevTile, tile);
      return true;
    }

    // Some state stuff might need to be remembered
    Tile newTile = prevTile.updatedTile(tile);
    replaceTile(prevTile, newTile);
    if (tile instanceof Walkable != prevTile instanceof Walkable) {
      return true;
    }

    if (!(tile instanceof Walkable)) {
      return false;
    }

    return ((Walkable) prevTile).getWalkableType() != ((Walkable) tile).getWalkableType();
  }

  public void updatePassibility(Tile tile) {
    Cluster cluster = hierarchicalMap.findClusterForPosition(tile.pos);
    CustomVec2D relPos = cluster.toRelativePos(tile.pos);

    // Update main concreteMap
    boolean canMoveDiagonally =
        tile instanceof Walkable
            && ((Walkable) tile).getWalkableType() == Walkable.WalkableType.Diagonal;
    boolean isObstacle = !(tile instanceof Walkable);
    concreteMap.passability.updateCanMoveDiagonally(tile.pos, canMoveDiagonally);
    concreteMap.passability.updateObstacle(tile.pos, isObstacle);

    // Update subConcreteMaps
    cluster.subConcreteMap.passability.updateCanMoveDiagonally(relPos, canMoveDiagonally);
    cluster.subConcreteMap.passability.updateObstacle(relPos, isObstacle);
  }

  private void replaceTile(Tile oldTile, Tile newTile) {
    // Remove old tile from the list of tiles
    if (oldTile != null) {
      String oldTypeName = oldTile.getClass().getName();
      if (tileTypes.containsKey(oldTypeName)) {
        tileTypes.get(oldTypeName).remove(oldTile.pos);
      }
    }

    // Add tile to object types
    String newTypeName = newTile.getClass().getName();
    tileTypes.putIfAbsent(newTypeName, new HashSet<>());
    tileTypes.get(newTypeName).add(newTile.pos);
    tiles[newTile.pos.y][newTile.pos.x] = newTile;
  }

  public HashSet<CustomVec2D> getCoordinatesOfTileType(Class tileClass) {
    return tileTypes.get(tileClass.getName());
  }

  public boolean nullTile(CustomVec2D pos) {
    return getTile(pos) == null;
  }

  public Tile getTile(CustomVec2D pos) {
    return tiles[pos.y][pos.x];
  }

  // region XPathfinder interface
  public boolean hasBeenSeen(CustomVec2D pos) {
    Tile t = getTile(pos);
    return t != null && t.seen;
  }

  //  @Override
  public void markAsSeen(List<CustomVec2D> newlySeen) {
    newlySeen.forEach(this::markAsSeen);
  }

  //  @Override
  public void markAsSeen(CustomVec2D p) {
    Tile t = getTile(p);
    assert t != null;
    t.seen = true;
  }

  public boolean isWalkable(CustomVec2D pos) {
    return isWalkable(getTile(pos));
  }

  private boolean isWalkable(Tile tile) {
    return tile instanceof Walkable && ((Walkable) tile).isWalkable();
  }

  /**
   * This returns the set of frontier-tiles. A tile is a frontier tile if it is a seen/explored
   * tile, and it has at least one unexplored and unblocked neighbor. Note that under this
   * definition a frontier does not have to be reachable. You can use findPath to check which
   * frontiers are reachable.
   */
  public List<CustomVec2D> getFrontier() {
    List<CustomVec2D> removingFrontiers = new ArrayList<>(frontiers.size());

    for (CustomVec2D frontier : frontiers) {
      Tile tile = getTile(frontier);
      List<CustomVec2D> neighbours = neighbourCoordinates(frontier, true);
      if (neighbours.stream().allMatch(pos -> getTile(pos) == null || getTile(pos).seen)) {
        removingFrontiers.add(frontier);
      }
    }

    removingFrontiers.forEach(frontiers::remove);

    for (int x = 0; x < hierarchicalMap.size.width; x++) {
      for (int y = 0; y < hierarchicalMap.size.height; y++) {
        CustomVec2D pos = new CustomVec2D(x, y);
        if (frontiers.contains(pos)) {
          continue;
        }
        Tile tile = getTile(pos);
        if (tile == null || !tile.seen || !(tile instanceof Walkable)) {
          continue;
        }

        // Shop doors also are not frontiers
        if (tile instanceof Door && ((Door) tile).isShopDoor) {
          continue;
        }

        Viewable viewable = (Viewable) tile;
        if (!viewable.isVisible()) {
          continue;
        }

        List<CustomVec2D> neighbours = neighbourCoordinates(pos, true);
        if (neighbours.stream()
            .anyMatch(
                neighbourPos ->
                    getTile(neighbourPos) != null
                        && !getTile(neighbourPos).seen
                        && getTile(neighbourPos) instanceof Walkable)) {
          frontiers.add(pos);
        }
      }
    }

    return new ArrayList<>(frontiers);
  }

  public Path<CustomVec2D> explore(CustomVec2D startingLocation, CustomVec2D heuristicLocation) {
    return explore(
        startingLocation.x, startingLocation.y, heuristicLocation.x, heuristicLocation.y);
  }

  private Path<CustomVec2D> explore(int x, int y, int heuristicX, int heuristicY) {
    CustomVec2D startPos = new CustomVec2D(x, y);
    List<CustomVec2D> frontiers = getFrontier();
    frontiers.removeIf(pos -> pos.equals(startPos));
    if (frontiers.isEmpty()) {
      return null;
    }

    CustomVec2D heuristicPos = new CustomVec2D(heuristicX, heuristicY);
    // sort the frontiers in ascending order, by their geometric distance to (x,y):
    frontiers.sort((p1, p2) -> Float.compare(heuristic(p1, startPos), heuristic(p2, heuristicPos)));

    for (CustomVec2D front : frontiers) {
      Path<CustomVec2D> path = findPath(x, y, front.x, front.y);
      if (path != null) {
        return path;
      }
    }
    return null;
  }

  public Path<CustomVec2D> findPath(CustomVec2D from, CustomVec2D to) {
    return findPaths(from, Collections.singletonList(to)).get(0);
  }

  public List<Path<CustomVec2D>> findPaths(CustomVec2D from, List<CustomVec2D> targets) {
    assert !targets.isEmpty() : "Cannot find shortest path to zero targets";
    HierarchicalMapFactory factory = new HierarchicalMapFactory();
    Id<AbstractNode> startAbsNode = factory.insertAbstractNode(hierarchicalMap, from);
    int maxPathsToRefine = Integer.MAX_VALUE;
    HierarchicalSearch hierarchicalSearch = new HierarchicalSearch();
    List<Path<CustomVec2D>> result = new ArrayList<>(targets.size());
    for (CustomVec2D target : targets) {
      if (from.equals(target)) {
        result.add(new Path<>());
        continue;
      }

      Id<AbstractNode> targetAbsNode = factory.insertAbstractNode(hierarchicalMap, target);
      List<AbstractPathNode> abstractPath =
          hierarchicalSearch.doHierarchicalSearch(
              hierarchicalMap, startAbsNode, targetAbsNode, 1, maxPathsToRefine);

      if (abstractPath == null) {
        result.add(null);
      } else {
        List<IPathNode> path =
            hierarchicalSearch.abstractPathToLowLevelPath(
                hierarchicalMap, abstractPath, hierarchicalMap.size.width, maxPathsToRefine);
        // Instead of smooth, optimize path by taking shortcuts
        Path<CustomVec2D> posPath = toPositionPath(path, concreteMap);
        assert isValidPath(from, target, posPath);
        result.add(posPath);
      }
      factory.removeAbstractNode(hierarchicalMap, targetAbsNode);
    }

    factory.removeAbstractNode(hierarchicalMap, startAbsNode);
    return result;
  }

  // Has optimizations in place to reduce the amount of time to find the shortest path
  public Path<CustomVec2D> findShortestPath(CustomVec2D from, List<CustomVec2D> targets) {
    assert !targets.isEmpty() : "Cannot find shortest path to zero targets";

    targets = from.sortBasedOnManhattanDistance(targets);
    // Closest destination same as the target, path is empty
    if (targets.get(0).equals(from)) {
      return new Path<>();
    }

    HierarchicalMapFactory factory = new HierarchicalMapFactory();
    Id<AbstractNode> startAbsNode = factory.insertAbstractNode(hierarchicalMap, from);
    int maxPathsToRefine = Integer.MAX_VALUE;
    HierarchicalSearch hierarchicalSearch = new HierarchicalSearch();
    Path<CustomVec2D> shortestPath = null;

    for (CustomVec2D target : targets) {
      if (shortestPath == null) {
        // Computation must be performed since any path is shortest
      } else if (CustomVec2D.manhattan(from, target) * Constants.COST_ONE >= shortestPath.cost) {
        break;
      }

      Id<AbstractNode> targetAbsNode = factory.insertAbstractNode(hierarchicalMap, target);
      List<AbstractPathNode> abstractPath =
          hierarchicalSearch.doHierarchicalSearch(
              hierarchicalMap, startAbsNode, targetAbsNode, 1, maxPathsToRefine);

      if (abstractPath != null) {
        List<IPathNode> path =
            hierarchicalSearch.abstractPathToLowLevelPath(
                hierarchicalMap, abstractPath, hierarchicalMap.size.width, maxPathsToRefine);
        // Instead of smooth, optimize path by taking shortcuts
        Path<CustomVec2D> posPath = toPositionPath(path, concreteMap);
        assert isValidPath(from, target, posPath);
        if (shortestPath == null || posPath.cost < shortestPath.cost) {
          shortestPath = posPath;
        }
      }
      factory.removeAbstractNode(hierarchicalMap, targetAbsNode);
    }

    factory.removeAbstractNode(hierarchicalMap, startAbsNode);
    return shortestPath;
  }

  private Path<CustomVec2D> toPositionPath(List<IPathNode> path, ConcreteMap concreteMap) {
    List<CustomVec2D> nodes =
        path.stream()
            .map(
                (p) -> {
                  if (p instanceof ConcretePathNode) {
                    ConcretePathNode concretePathNode = (ConcretePathNode) p;
                    return concreteMap.graph.getNodeInfo(concretePathNode.id).position;
                  }

                  AbstractPathNode abstractPathNode = (AbstractPathNode) p;
                  return hierarchicalMap.abstractGraph.getNodeInfo(abstractPathNode.id).position;
                })
            .collect(Collectors.toList());
    return new Path<>(nodes, (nodes.size() - 1) * Constants.COST_ONE);
  }

  public boolean isValidPath(CustomVec2D from, CustomVec2D to, Path<CustomVec2D> path) {
    if (path.atLocation()) {
      assert from.equals(to) : "If path is empty, it must be a path to itself";
      return true;
    }

    assert !from.equals(to);
    List<CustomVec2D> nodes = path.nodes;

    assert nodes.get(0).equals(from) : "Path from or to is incorrect";
    int n = nodes.size();
    assert nodes.get(n - 1).equals(to) : "Path to is incorrect";

    CustomVec2D prevPos = nodes.get(0);
    for (int i = 1; i < n; i++) {
      CustomVec2D currentPos = nodes.get(i);
      assert CustomVec2D.adjacent(prevPos, currentPos, true)
          : String.format("Non adjacent node error at %s -> %s", prevPos, currentPos);
      prevPos = currentPos;
    }

    return true;
  }

  public Path<CustomVec2D> findPath(int fromX, int fromY, int toX, int toY) {
    return findPath(new CustomVec2D(fromX, fromY), new CustomVec2D(toX, toY));
  }

  /** When true then the pathfinder will consider all nodes in the graph to have been seen. */
  public boolean usingPerfectMemoryPathfinding() {
    return perfect_memory_pathfinding;
  }

  /** When true then the pathfinder will consider all nodes in the graph to have been seen. */
  public void setPerfectMemoryPathfinding(Boolean flag) {
    perfect_memory_pathfinding = flag;
  }

  /** Mark all vertices as "unseen". */
  public void wipeOutMemory() {
    for (Tile[] row : tiles) {
      for (Tile t : row) {
        if (t != null) {
          t.seen = false;
        }
      }
    }
  }
  // endregion

  // region Navigatable interface

  /**
   * Return the neighbors of a tile. A tile u is a neighbor of a tile t if u is adjacent to t, and
   * moreover u is navigable (e.g. it is not a wall or a closed door). If the flag
   * diagonalMovementPossible is true, then tiles that are diagonally touching t are also considered
   * neighbors.
   *
   * <p>Only neighbors that have been seen before will be included.
   */
  @Override
  public Iterable<CustomVec2D> neighbours(CustomVec2D pos) {
    Tile tile = getTile(pos);
    if (tile == null) {
      return new ArrayList<>();
    }
    return neighbours_(tile.pos);
  }

  public List<CustomVec2D> neighbourCoordinates(CustomVec2D pos, boolean allowDiagonal) {
    return NavUtils.neighbourCoordinates(pos, hierarchicalMap.size, allowDiagonal);
  }

  /**
   * Return the neighbors of a tile. A tile u is a neighbor of a tile t if u is adjacent to t, and
   * moreover u is navigable (e.g. it is not a wall or a closed door). If the flag
   * diagonalMovementPossible is true, then tiles that are diagonally touching t are also considered
   * neighbors.
   *
   * <p>Only neighbors that have been seen before will be included.
   *
   * <p>For optimization purposes Lists with filters or streams have been avoided Instead arrays are
   * used and at the end a list is built
   */
  private List<CustomVec2D> neighbours_(CustomVec2D pos) {
    boolean allowDiagonal = getTile(pos) instanceof Walkable;
    List<CustomVec2D> candidates = neighbourCoordinates(pos, allowDiagonal);

    int nrResults = 0;
    boolean[] toNeighbour = new boolean[candidates.size()];

    for (int i = 0; i < candidates.size(); i++) {
      CustomVec2D candidate = candidates.get(i);
      toNeighbour[i] = isWalkable(getTile(candidate));
      if (!perfect_memory_pathfinding) {
        toNeighbour[i] = toNeighbour[i] && hasBeenSeen(candidate);
      }
      if (toNeighbour[i]) {
        nrResults++;
      }
    }

    List<CustomVec2D> result = new ArrayList<>(nrResults);
    for (int i = 0; i < candidates.size(); i++) {
      if (toNeighbour[i]) {
        result.add(candidates.get(i));
      }
    }
    return result;
  }

  /** The estimated distance between two arbitrary vertices. */
  public float heuristic(CustomVec2D p, CustomVec2D q) {
    return CustomVec2D.distSq(p, q);
  }

  @Override
  public float distance(CustomVec2D p, CustomVec2D q) {
    return heuristic(p, q);
  }
  // endregion

  @Override
  public String toString() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    Set<CustomVec2D> frontiers = new HashSet<>(getFrontier());
    // Add row by row to the StringBuilder
    for (int y = 0; y < hierarchicalMap.size.height; y++) {
      for (int x = 0; x < hierarchicalMap.size.width; x++) {
        // Get tile, if it doesn't know the type it is not know or void.
        CustomVec2D pos = new CustomVec2D(x, y);
        Tile t = getTile(pos);
        boolean isFrontier = frontiers.contains(pos);

        String colorString;
        if (isFrontier) {
          colorString = "\033[103m";
        } else {
          colorString = Color.RESET.toString();
        }

        if (t == null) {
          csb.append(' ');
        } else {
          csb.append(colorString).append(t.toChar());
        }
      }

      // Don't add line after last row
      if (y != hierarchicalMap.size.height - 1) {
        csb.newLine();
      }
    }
    return csb.toString();
  }
}
