package agent.navigation;
;
import agent.navigation.hpastar.*;
import agent.navigation.hpastar.factories.EntranceStyle;
import agent.navigation.hpastar.factories.HierarchicalMapFactory;
import agent.navigation.hpastar.graph.*;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.passabilities.EmptyPassability;
import agent.navigation.hpastar.search.HierarchicalSearch;
import agent.navigation.hpastar.smoother.Direction;
import agent.navigation.hpastar.smoother.SmoothWizard;
import agent.navigation.hpastar.utils.RefSupport;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.*;
import eu.iv4xr.framework.extensions.pathfinding.Navigatable;
import eu.iv4xr.framework.extensions.pathfinding.XPathfinder;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.*;
import java.util.stream.Collectors;
import nethack.enums.Color;
import org.apache.logging.log4j.Logger;
import util.ColoredStringBuilder;
import util.Loggers;

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
public class GridSurface implements Navigatable<Tile>, XPathfinder<Tile> {
  static final Logger logger = Loggers.NavLogger;
  public final Tile[][] tiles;
  public final Map<String, HashSet<IntVec2D>> tileTypes = new HashMap<>();
  public final Set<Tile> frontierCandidates = new HashSet<>();
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
    ConcreteMap emptyConcreteMap = new EmptyPassability(size).getConcreteMap();
    concreteMap = emptyConcreteMap;
    hierarchicalMap =
        new HierarchicalMapFactory()
            .createHierarchicalMap(
                emptyConcreteMap, clusterSize, 1, EntranceStyle.EndEntrance, size);
  }

  public IntVec2D toRelativePos(IntVec2D pos) {
    assert NavUtils.withinBounds(pos, hierarchicalMap.size);
    return new IntVec2D(pos.x % hierarchicalMap.clusterSize, pos.y % hierarchicalMap.clusterSize);
  }

  public void updateTiles(List<Tile> newTiles, List<IntVec2D> toggleOffBlocking) {
    Map<Cluster, Set<Direction>> entrances = new HashMap<>();

    for (IntVec2D pos : toggleOffBlocking) {
      Tile tile = getTile(pos);
      if (tile instanceof Door) {
        Door door = (Door) tile;
        door.setBlockingState(false);
      }
    }

    for (Tile tile : newTiles) {
      boolean updated = updateTile(tile);
      markAsSeen(tile);
      if (!updated) {
        continue;
      }

      boolean blocked = !(tile instanceof StraightWalkable);
      if (blocked) {
        GridSurfaceFactory.removeEdges(this, tile);
        continue;
      }

      Set<Direction> directions = GridSurfaceFactory.addEdges(this, tile);
      addClusterEdges(entrances, tile, directions);
    }

    for (Cluster cluster : entrances.keySet()) {
      System.out.printf("Cluster directions: %s %s%n", cluster.id, entrances.get(cluster));
    }

    createEntrances(entrances);
  }

  private void addClusterEdges(
      Map<Cluster, Set<Direction>> entrances, Tile tile, Set<Direction> directions) {
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
      if (!entrances.containsKey(mapCluster)) {
        entrances.put(mapCluster, new HashSet<>());
      }
      entrances.get(mapCluster).add(mapDirection);
    }
  }

  private void createEntrances(Map<Cluster, Set<Direction>> entrances) {
    for (Cluster cluster : entrances.keySet()) {
      int top = cluster.origin.y;
      int left = cluster.origin.x;
      for (Direction direction : entrances.get(cluster)) {
        Cluster neighbourCluster =
            GridSurfaceFactory.getClusterInDirection(this, cluster, direction);
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

      cluster.createIntraClusterEdges();
      GridSurfaceFactory.createIntraClusterEdges(this, cluster);
    }
  }

  private boolean updateTile(Tile tile) {
    updatePassibility(tile);
    Tile prevTile = tiles[tile.pos.y][tile.pos.x];
    replaceTile(prevTile, tile);
    tiles[tile.pos.y][tile.pos.x] = tile;
    return tile instanceof StraightWalkable != prevTile instanceof StraightWalkable
        || tile instanceof Walkable != prevTile instanceof Walkable;
  }

  public void updatePassibility(Tile tile) {
    Cluster cluster = hierarchicalMap.findClusterForPosition(tile.pos);
    IntVec2D relPos = toRelativePos(tile.pos);

    // Update main concreteMap
    concreteMap.passability.updateCanMoveDiagonally(tile.pos, tile instanceof Walkable);
    concreteMap.passability.updateObstacle(tile.pos, !(tile instanceof StraightWalkable));

    // Update subConcreteMaps
    cluster.subConcreteMap.passability.updateCanMoveDiagonally(relPos, tile instanceof Walkable);
    cluster.subConcreteMap.passability.updateObstacle(relPos, !(tile instanceof StraightWalkable));
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
  }

  public HashSet<IntVec2D> getCoordinatesOfTileType(Class tileClass) {
    return tileTypes.get(tileClass.getName());
  }

  public boolean nullTile(IntVec2D pos) {
    return getTile(pos) == null;
  }

  public Tile getTile(IntVec2D pos) {
    return tiles[pos.y][pos.x];
  }

  // region XPathfinder interface
  @Override
  public boolean hasbeenSeen(Tile tile) {
    return hasbeenSeen(tile.pos);
  }

  private boolean hasbeenSeen(IntVec2D pos) {
    Tile t = getTile(pos);
    return t != null && t.seen;
  }

  @Override
  public void markAsSeen(List<Tile> newlySeen) {
    newlySeen.forEach(this::markAsSeen);
  }

  @Override
  public void markAsSeen(Tile p) {
    Tile t = getTile(p.pos);
    assert t != null;
    t.seen = true;
    // Add as frontier if it is walkable
    if (isWalkable(t)) {
      frontierCandidates.add(p);
    }
  }

  public boolean isWalkable(IntVec2D pos) {
    return isWalkable(getTile(pos));
  }

  private boolean isWalkable(Tile tile) {
    return tile instanceof StraightWalkable && ((StraightWalkable) tile).isWalkable();
  }

  /**
   * This returns the set of frontier-tiles. A tile is a frontier tile if it is a seen/explored
   * tile, and it has at least one unexplored and unblocked neighbor. Note that under this
   * definition a frontier does not have to be reachable. You can use findPath to check which
   * frontiers are reachable.
   */
  public List<Tile> getFrontier() {
    List<Tile> frontiers = new LinkedList<>();
    List<Tile> cannotBeFrontier = new LinkedList<>();
    for (Tile t : frontierCandidates) {
      List<IntVec2D> pneighbors = NavUtils.neighbourCoordinates(t.pos, hierarchicalMap.size, true);
      boolean isFrontier = false;
      for (IntVec2D n : pneighbors) {
        if (!hasbeenSeen(n)) {
          frontiers.add(t);
          isFrontier = true;
          break;
        }
      }
      if (!isFrontier) {
        cannotBeFrontier.add(t);
      }
    }
    cannotBeFrontier.forEach(frontierCandidates::remove);
    return frontiers;
  }

  public List<Tile> explore(Tile startingLocation, Tile heuristicLocation) {
    return explore(
        startingLocation.pos.x,
        startingLocation.pos.y,
        heuristicLocation.pos.x,
        heuristicLocation.pos.y);
  }

  private List<Tile> explore(int x, int y, int heuristicX, int heuristicY) {
    List<Tile> frontiers = getFrontier();
    if (frontiers.isEmpty()) {
      return null;
    }

    IntVec2D startPos = new IntVec2D(x, y);
    IntVec2D heuristicPos = new IntVec2D(heuristicX, heuristicY);

    // sort the frontiers in ascending order, by their geometric distance to (x,y):
    frontiers.sort(
        (p1, p2) -> Float.compare(heuristic(p1.pos, startPos), heuristic(p2.pos, heuristicPos)));

    for (Tile front : frontiers) {
      List<Tile> path = findPath(x, y, front.pos.x, front.pos.y);
      if (path != null) {
        return path;
      }
    }
    return null;
  }

  @Override
  public List<Tile> findPath(Tile from, Tile to) {
    HierarchicalMapFactory factory = new HierarchicalMapFactory();
    Id<AbstractNode> startAbsNode = factory.insertAbstractNode(hierarchicalMap, from.pos);
    Id<AbstractNode> targetAbsNode = factory.insertAbstractNode(hierarchicalMap, to.pos);
    assert !startAbsNode.equals(targetAbsNode);
    int maxPathsToRefine = Integer.MAX_VALUE;
    HierarchicalSearch hierarchicalSearch = new HierarchicalSearch();
    List<AbstractPathNode> abstractPath =
        hierarchicalSearch.doHierarchicalSearch(
            hierarchicalMap, startAbsNode, targetAbsNode, 1, maxPathsToRefine);
    List<IPathNode> path =
        hierarchicalSearch.abstractPathToLowLevelPath(
            hierarchicalMap, abstractPath, hierarchicalMap.size.width, maxPathsToRefine);

    SmoothWizard smoother = new SmoothWizard(concreteMap, path);
    path = smoother.smoothPath();
    factory.removeAbstractNode(hierarchicalMap, targetAbsNode);
    factory.removeAbstractNode(hierarchicalMap, startAbsNode);

    List<IntVec2D> posPath = toPositionPath(path, concreteMap);
    Loggers.HPALogger.info("FindPath: %s -> %s (%s)", from, to, posPath);
    verifyPath(from.pos, to.pos, posPath);
    return posPath.stream().map(Tile::new).collect(Collectors.toList());
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
              return hierarchicalMap.abstractGraph.getNodeInfo(abstractPathNode.id).position;
            })
        .collect(Collectors.toList());
  }

  private void verifyPath(IntVec2D from, IntVec2D to, List<IntVec2D> path) {
    assert !from.equals(to) : "Path to itself";
    if (path.isEmpty()) {
      return;
    }

    assert path.get(0).equals(from) : "Path from or to is incorrect";
    assert path.get(path.size() - 1).equals(to) : "Path to is incorrect";

    IntVec2D prevPos = path.get(0);
    for (int i = 1; i < path.size(); i++) {
      IntVec2D currentPos = path.get(i);
      assert NavUtils.adjacent(prevPos, currentPos, true)
          : String.format("Non adjacent node error at %s -> %s", prevPos, currentPos);
      prevPos = currentPos;
    }
  }

  public List<Tile> findPath(int fromX, int fromY, int toX, int toY) {
    return findPath(new Tile(fromX, fromY), new Tile(toX, toY));
  }

  /** When true then the pathfinder will consider all nodes in the graph to have been seen. */
  @Override
  public boolean usingPerfectMemoryPathfinding() {
    return perfect_memory_pathfinding;
  }

  /** When true then the pathfinder will consider all nodes in the graph to have been seen. */
  @Override
  public void setPerfectMemoryPathfinding(Boolean flag) {
    perfect_memory_pathfinding = flag;
  }

  /** Mark all vertices as "unseen". */
  @Override
  public void wipeOutMemory() {
    for (Tile[] row : tiles) {
      for (Tile t : row) {
        if (t != null) {
          t.seen = false;
        }
      }
    }
    frontierCandidates.clear();
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
  public Iterable<Tile> neighbours(Tile t) {
    Tile tile = getTile(t.pos);
    if (tile == null) {
      return new ArrayList<>();
    }
    return neighbours_(tile.pos);
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
  private List<Tile> neighbours_(IntVec2D pos) {
    boolean allowDiagonal = getTile(pos) instanceof Walkable;
    List<IntVec2D> candidates =
        NavUtils.neighbourCoordinates(pos, hierarchicalMap.size, allowDiagonal);

    int nrResults = 0;
    boolean[] toNeighbour = new boolean[candidates.size()];

    for (int i = 0; i < candidates.size(); i++) {
      IntVec2D candidate = candidates.get(i);
      toNeighbour[i] = isWalkable(getTile(candidate));
      if (!perfect_memory_pathfinding) {
        toNeighbour[i] = toNeighbour[i] && hasbeenSeen(candidate);
      }
      if (toNeighbour[i]) {
        nrResults++;
      }
    }

    List<Tile> result = new ArrayList<>(nrResults);
    for (int i = 0; i < candidates.size(); i++) {
      if (toNeighbour[i]) {
        result.add(new Tile(candidates.get(i)));
      }
    }
    return result;
  }

  /** The estimated distance between two arbitrary vertices. */
  public float heuristic(Tile from, Tile to) {
    return heuristic(from.pos, to.pos);
  }

  public static float heuristic(IntVec2D from, IntVec2D to) {
    int dx = Math.abs(from.x - to.x);
    int dy = Math.abs(from.y - to.y);
    return Math.max(dx, dy);
  }

  /** The distance between two neighboring tiles. */
  public float distance(Tile from, Tile to) {
    return 1;
  }
  // endregion

  @Override
  public String toString() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    // Add row by row to the StringBuilder
    for (int y = 0; y < hierarchicalMap.size.height; y++) {
      for (int x = 0; x < hierarchicalMap.size.width; x++) {
        // Get tile, if it doesn't know the type it is not know or void.
        Tile t = getTile(new IntVec2D(x, y));

        if (t instanceof Printable) {
          Printable p = (Printable) t;
          char c = p.toChar();
          if (p.isVisible()) {
            csb.append(Color.GREEN_BRIGHT, c);
          } else {
            csb.append(c);
          }
        } else if (t == null) {
          csb.append(' ');
        } else {
          csb.append('?');
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
