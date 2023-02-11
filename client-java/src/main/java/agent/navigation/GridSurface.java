package agent.navigation;

import agent.AgentLoggers;
import agent.navigation.hpastar.*;
import agent.navigation.hpastar.factories.EntranceStyle;
import agent.navigation.hpastar.factories.HierarchicalMapFactory;
import agent.navigation.hpastar.graph.AbstractNode;
import agent.navigation.hpastar.infrastructure.Id;
import agent.navigation.hpastar.passabilities.EmptyPassability;
import agent.navigation.hpastar.search.HierarchicalSearch;
import agent.navigation.hpastar.smoother.SmoothWizard;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.*;
import eu.iv4xr.framework.extensions.pathfinding.CanDealWithDynamicObstacle;
import eu.iv4xr.framework.extensions.pathfinding.Navigatable;
import eu.iv4xr.framework.extensions.pathfinding.XPathfinder;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Logger;

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
public class GridSurface
    implements Navigatable<Tile>, XPathfinder<Tile>, CanDealWithDynamicObstacle<Tile> {
  static final Logger logger = AgentLoggers.NavLogger;
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

  private final ConcreteMap concreteMap;
  private final Size size;
  private final int clusterSize;

  public GridSurface(Size size, int clusterSize) {
    this.size = size;
    this.clusterSize = clusterSize;
    tiles = new Tile[size.height][size.width];
    ConcreteMap emptyConcreteMap = new EmptyPassability(size).getConcreteMap();
    concreteMap = emptyConcreteMap;
    hierarchicalMap =
        new HierarchicalMapFactory()
            .createHierarchicalMap(
                emptyConcreteMap, clusterSize, 1, EntranceStyle.EndEntrance, size);
  }

  // region CanDealWithDynamicObstacle interface
  /** Add a non-navigable tile (obstacle). */
  @Override
  public void addObstacle(Tile o) {
    updatePassibility(o);
    assert !(o instanceof StraightWalkable)
        : "Obstacle is not actually an obstacle since it can be passed";
    Tile oldTile = tiles[o.pos.y][o.pos.x];
    replaceTile(oldTile, o);
    tiles[o.pos.y][o.pos.x] = o;
  }

  /** Remove a non-navigable tile (obstacle). */
  @Override
  public void removeObstacle(Tile o) {
    updatePassibility(o);
    assert o instanceof StraightWalkable : "RemoveObstacle must insert a walkable tile";
    Tile oldTile = tiles[o.pos.y][o.pos.x];
    replaceTile(oldTile, o);
    tiles[o.pos.y][o.pos.x] = o;
    if (oldTile instanceof StraightWalkable
        && ((StraightWalkable) oldTile).isWalkable()
        && oldTile instanceof Walkable == o instanceof Walkable) {
      return;
    }
  }

  public void updatePassibility(Tile tile) {
    Cluster cluster = hierarchicalMap.findClusterForPosition(tile.pos);
    IntVec2D relPos = new IntVec2D(tile.pos.x % clusterSize, tile.pos.y % clusterSize);

    // Update main concreteMap
    concreteMap.passability.updateCanMoveDiagonally(tile.pos, tile instanceof Walkable);
    concreteMap.passability.updateObstacle(tile.pos, !(tile instanceof StraightWalkable));

    // Update subconcreteMaps
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

  /**
   * The tile is blocking (true) if it is a wall or a closed door. Else it is non-blocking (false).
   */
  @Override
  public boolean isBlocking(Tile tile) {
    return isBlocking(tile.pos);
  }

  public boolean isBlocking(IntVec2D pos) {
    Tile t = getTile(pos);
    return !(t instanceof StraightWalkable) || !((StraightWalkable) t).isWalkable();
  }

  /**
   * Set the blocking state of this tile, if it is a Door, to the given blocking-state (true means
   * blocking, false non-blocking).
   *
   * <p>If the tile is not a Door, this method has no effect.
   */
  @Override
  public void setBlockingState(Tile tile, boolean isBlocking) {
    Tile t = getTile(tile.pos);
    if (t instanceof Door) {
      Door door = (Door) t;
      door.setBlockingState(isBlocking);
    }
  }
  // endregion

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
    // Do not mark frontiers around tiles that are blocking
    if (!isBlocking(t)) {
      frontierCandidates.add(p);
    }
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
      List<IntVec2D> pneighbors = NavUtils.neighbourCoordinates(t.pos, size, true);
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

    assert path.get(0).equals(from) && path.get(path.size() - 1).equals(to)
        : "Path from or to is empty";

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
    List<IntVec2D> candidates = NavUtils.neighbourCoordinates(pos, size, allowDiagonal);

    int nrResults = 0;
    boolean[] toNeighbour = new boolean[candidates.size()];

    for (int i = 0; i < candidates.size(); i++) {
      IntVec2D candidate = candidates.get(i);
      toNeighbour[i] = !isBlocking(candidate);
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
    StringBuilder sb = new StringBuilder();
    // Add row by row to the StringBuilder
    for (int y = 0; y < size.height; y++) {
      for (int x = 0; x < size.width; x++) {
        // Get tile, if it doesn't know the type it is not know or void.
        Tile t = getTile(new IntVec2D(x, y));
        if (t == null) {
          sb.append(' ');
        } else if (!(t instanceof Printable)) {
          sb.append('?');
        } else {
          sb.append(((Printable) t).toColoredString());
        }
      }

      // Don't add line after last row
      if (y != size.height - 1) {
        sb.append(System.lineSeparator());
      }
    }
    return sb.toString();
  }
}
