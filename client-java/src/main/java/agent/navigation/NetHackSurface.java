package agent.navigation;

import agent.AgentLoggers;
import agent.navigation.surface.*;
import alice.tuprolog.Int;
import eu.iv4xr.framework.extensions.pathfinding.*;
import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import nethack.object.Color;
import nethack.object.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Representing a navigation graph over a 2D tiled-world. The world is assumed
 * to be made of tiles/squares, arranged from tile (0,0) to tile (maxX-1,maxY-1)
 * to form a rectangle world.
 *
 * <p>
 * The tiles are not explicitly stored. Rather, we only store non-navigable
 * tiles. These are tiles that block movement through them. There are two types:
 * Wall and Door. A wall is always non-navigable. A door can be made
 * blocking/unblocking.
 *
 * <p>
 * The class also implements {@link Navigatable} and {@link XPathfinder}, so it offers methods to do
 * pathfinding and exploration over the world.
 *
 * @author Wish
 */
public class NetHackSurface implements Navigatable<Tile>, XPathfinder<Tile>, CanDealWithDynamicObstacle<Tile> {
    public Pathfinder<Tile> pathfinder = new AStar<>();

    static final Logger logger = LogManager.getLogger(AgentLoggers.NavLogger);
    static final int sizeX = Level.WIDTH, sizeY = Level.HEIGHT;
    public final Tile[][] tiles = new Tile[sizeY + 2][sizeX + 2];
    /**
     * If true, the pathfinder will assume that the whole NavGraph has been "seen",
     * so no vertex would count as unreachable because it is still unseen. This
     * essentially turns off memory-based path finding. The default of this flag is
     * false.
     */
    boolean perfect_memory_pathfinding = true;
    Set<Tile> frontierCandidates = new HashSet<>();

    private boolean isDiagonalDoorMove(IntVec2D pos0, IntVec2D pos1) {
        if (pos0.x == pos1.x || pos0.y == pos1.y) {
            return false;
        }
        return isDoor(pos0) || isDoor(pos1);
    }

    private boolean isDoor(IntVec2D pos) {
        Obstacle o = getObstacle(pos);
        if (o == null)
            return false;
        return o instanceof Door;
    }

    private boolean isFloor(IntVec2D pos) {
        Tile o = getFloor(pos);
        if (o == null)
            return false;
        return o instanceof Floor;
    }

    private boolean blocksVision(IntVec2D pos) { return blocksVision(getTile(pos)); }

    private boolean blocksVision(Tile t) { return !t.seeThrough; }

    public boolean hasTile(IntVec2D pos) { return getTile(pos) != null; }

    public Tile getTile(IntVec2D pos) {
        return getTile(pos.x, pos.y);
    }

    private Tile getTile(int x, int y) {
        return tiles[y + 1][x + 1];
    }

    private Obstacle getObstacle(IntVec2D pos) {
        Tile t = getTile(pos);
        if (t instanceof Obstacle) {
            return (Obstacle)t;
        }
        return null;
    }

    private Tile getFloor(IntVec2D pos) {
        Tile t = getTile(pos);
        if (t != null && !(t instanceof Obstacle)) {
            return t;
        }
        return null;
    }

    public List<IntVec2D> VisibleCoordinates(IntVec2D agentPosition, Level level) {
        resetVisibility();

        // Perform BFS on the graph, initiate the queue with the agent position and all the lit floor tiles
        HashSet<IntVec2D> visibleCoordinates = new HashSet<>();
        HashSet<IntVec2D> processedCoordinates = new HashSet<>();
        Queue<IntVec2D> queue = new LinkedList<>(level.visibleFloors);

        processedCoordinates.add(agentPosition);
        queue.addAll(Arrays.asList(physicalNeighbourCoordinates(agentPosition)));

        // While there are coordinates left to be explored
        while (queue.size() > 0) {
            IntVec2D nextPos = queue.remove();
            // Already processed
            if (processedCoordinates.contains(nextPos)) {
                continue;
            }
            processedCoordinates.add(nextPos);

            Tile t = getTile(nextPos);
            if (t == null) {
                continue;
            } else if (level.getEntity(nextPos).color == Color.TRANSPARENT) {
                continue;
            }

            // Get the neighbours
            IntVec2D[] neighbours = physicalNeighbourCoordinates(nextPos);
            if (t instanceof Doorway) {
                // Does not have a lit floor tile next to it, so we assume we cannot see it
                if (Arrays.stream(neighbours).noneMatch(coord -> isFloor(coord) && level.getEntity(coord).color != Color.TRANSPARENT)) {
                    continue;
                }
            }

            // Current tile is visible
            t.visible = true;
            visibleCoordinates.add(nextPos);

            // Do not add new candidates if the current one blocks vision
            if (!t.seeThrough) {
                continue;
            } // Only add all neighbours if it is floor or the current position of the agent
            else if (t instanceof Floor) {
                queue.addAll(Arrays.asList(neighbours));
            }
        }

        return new ArrayList<>(visibleCoordinates);
    }

    private void resetVisibility() {
        // First reset visibility of all tiles to false
        for (Tile[] row: tiles) {
            for (Tile t: row) {
                if (t != null) {
                    t.visible = false;
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // Add row by row to the StringBuilder
        for (int y = 0; y < sizeY; y++) {
            for (int x = 0; x < sizeX; x++) {
                // Get tile, if it doesn't know the type it is not know or void.
                Tile t = getTile(new IntVec2D(x, y));
                if (t == null) {
                    sb.append(' ');
                } else if (t.visible) {
                    sb.append(Color.GREEN_BRIGHT.stringCode()).append(t.toChar()).append(Color.RESET.stringCode());
                } else {
                    sb.append(t.toChar());
                }
            }

            // Don't add line after last row
            if (y != sizeY - 1) {
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    //region CanDealWithDynamicObstacle interface
    /**
     * Add a non-navigable tile (obstacle).
     */
    @Override
    public void addObstacle(Tile o) {
        if (!(o instanceof Obstacle)) {
            throw new IllegalArgumentException(String.format("Cannot add obstacle since %s is not one", o.getClass()));
        }

        tiles[o.pos.y + 1][o.pos.x + 1] = o;
        updateNeighbours(o);
    }

    private void updateNeighbours(Tile t) {
        t.neighbours = neighbours_(t.pos);
        for (IntVec2D neighbourCoordinate: physicalNeighbourCoordinates(t.pos)) {
            Tile neighbour = getTile(neighbourCoordinate);
            if (neighbour == null) {
                continue;
            }
            neighbour.neighbours = neighbours_(neighbour.pos);
        }
    }

    /**
     * Remove a non-navigable tile (obstacle).
     */
    @Override
    public void removeObstacle(Tile o) {
        tiles[o.pos.y + 1][o.pos.x + 1] = o;
        updateNeighbours(o);
    }

    /**
     * The tile is blocking (true) if it is a wall or a closed door. Else it is
     * non-blocking (false).
     */
    @Override
    public boolean isBlocking(Tile tile) {
        return isBlocking(tile.pos);
    }

    public boolean isBlocking(IntVec2D pos) {
        Tile t = getTile(pos);

        if (t instanceof Door) {
            Door d = (Door)t;
            return !d.isOpen;
        }
        return t instanceof Obstacle;
    }

    /**
     * Set the blocking state of this tile, if it is a Door, to the given
     * blocking-state (true means blocking, false non-blocking).
     * <p>
     * If the tile is not a Door, this method has no effect.
     */
    @Override
    public void setBlockingState(Tile tile, boolean isBlocking) {
        Tile t = getTile(tile.pos);
        if (t instanceof Door) {
            Door door = (Door) t;
            door.isOpen = !isBlocking;
        }
        updateNeighbours(t);
    }
    //endregion

    //region XPathfinder interface
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
        if (t != null) {
            t.seen = true;
            // Do not mark frontiers around tiles that are blocking
            if (!isBlocking(t)) {
                frontierCandidates.add(p);
            }
        } else {
            logger.warn(String.format("Tried to mark a null tile as seen @%s", p.pos));
            throw new IllegalStateException("Cannot try to mark a tile as seen which is not present");
        }
    }

    /**
     * This returns the set of frontier-tiles. A tile is a frontier tile if it is a
     * seen/explored tile, and it has at least one unexplored and unblocked neighbor.
     * Note that under this definition a frontier does not have to be reachable. You
     * can use findPath to check which frontiers are reachable.
     */
    public List<Tile> getFrontier() {
        List<Tile> frontiers = new LinkedList<>();
        List<Tile> cannotBeFrontier = new LinkedList<>();
        for (Tile t : frontierCandidates) {
            IntVec2D[] pneighbors = physicalNeighbourCoordinates(t.pos);
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
        return explore(startingLocation.pos.x, startingLocation.pos.y, heuristicLocation.pos.x, heuristicLocation.pos.y);
    }

    private List<Tile> explore(int x, int y, int heuristicX, int heuristicY) {
        List<Tile> frontiers = getFrontier();

        if (frontiers.isEmpty())
            return null;
        // sort the frontiers in ascending order, by their geometric distance to (x,y):
        frontiers.sort((p1, p2) -> Float.compare(distSq(p1.pos.x, p1.pos.y, x, y), distSq(p2.pos.x, p2.pos.y, heuristicX, heuristicY)));

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
        return pathfinder.findPath(this, from, to);
    }

    public List<Tile> findPath(int fromX, int fromY, int toX, int toY) {
        return findPath(new Tile(fromX, fromY), new Tile(toX, toY));
    }

    static float distSq(int x1, int y1, int x2, int y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    public static float distSq(IntVec2D pos0, IntVec2D pos1) {
        return distSq(pos0.x, pos0.y, pos1.x, pos1.y);
    }

    /**
     * When true then the pathfinder will consider all nodes in the graph to have
     * been seen.
     */
    @Override
    public boolean usingPerfectMemoryPathfinding() {
        return perfect_memory_pathfinding;
    }

    /**
     * When true then the pathfinder will consider all nodes in the graph to have
     * been seen.
     */
    @Override
    public void setPerfectMemoryPathfinding(Boolean flag) {
        perfect_memory_pathfinding = flag;
    }

    /**
     * Mark all vertices as "unseen".
     */
    @Override
    public void wipeOutMemory() {
        for (Tile[] row: tiles) {
            for (Tile t: row) {
                if (t != null) {
                    t.seen = false;
                }
            }
        }
        frontierCandidates.clear();
    }
    //endregion

    //region Navigatable interface
    /**
     * Return the neighbors of a tile. A tile u is a neighbor of a tile t if u is
     * adjacent to t, and moreover u is navigable (e.g. it is not a wall or a closed
     * door). If the flag diagonalMovementPossible is true, then tiles that are
     * diagonally touching t are also considered neighbors.
     *
     * <p>
     * Only neighbors that have been seen before will be included.
     */
    @Override
    public Iterable<Tile> neighbours(Tile t) {
        Tile tile = getTile(t.pos);
        if (tile == null) {
            return new ArrayList<>();
        }
        if (true) {
            return getTile(tile.pos).neighbours;
        } else {
            return neighbours_(tile.pos);
        }
    }

    public Iterable<Tile> neighboursDebug(Tile t) {
        Tile tile = getTile(t.pos);
        List<Tile> firstList = getTile(tile.pos).neighbours;
        List<Tile> secondList = neighbours_(tile.pos);

        if (firstList.size() != secondList.size()) {
            System.out.printf("-------%s-------%n", tile.pos);
            System.out.println("-------TILE LIST-------");
            for (Tile neighbour: firstList) {
                System.out.println(neighbour.pos);
            }

            System.out.println("-------TRUE LIST-------");
            for (Tile neighbour: secondList) {
                System.out.println(neighbour.pos);
            }
            System.out.println();
        }

        return secondList;
    }

    /**
     * Return the neighbors of a tile. A tile u is a neighbor of a tile t if u is
     * adjacent to t, and moreover u is navigable (e.g. it is not a wall or a closed
     * door). If the flag diagonalMovementPossible is true, then tiles that are
     * diagonally touching t are also considered neighbors.
     *
     * <p>
     * Only neighbors that have been seen before will be included.
     *
     * <p>
     * For optimization purposes Lists with filters or streams have been avoided
     * Instead arrays are used and at the end a list is built
     */
    private List<Tile> neighbours_(IntVec2D pos) {
        IntVec2D[] candidates = physicalNeighbourCoordinates(pos);
        int nrResults = 0;
        boolean[] toNeighbour = new boolean[candidates.length];

        for (int i = 0; i < candidates.length; i++) {
            IntVec2D candidate = candidates[i];
            toNeighbour[i] = !isBlocking(candidate) && !isDiagonalDoorMove(candidate, pos);
            if (!perfect_memory_pathfinding) {
                toNeighbour[i] = toNeighbour[i] && hasbeenSeen(candidate);
            }
            if (toNeighbour[i]) {
                nrResults++;
            }
        }

        List<Tile> result = new ArrayList<>(nrResults);
        for (int i = 0; i < candidates.length; i++) {
            if (toNeighbour[i]) {
                result.add(new Tile(candidates[i]));
            }
        }
        return result;
    }

    public static IntVec2D[] physicalNeighbourCoordinates(IntVec2D pos) {
        int left = pos.x - 1;
        int right = pos.x + 1;
        int below = pos.y - 1;
        int above = pos.y + 1;

        return new IntVec2D[]{
                new IntVec2D(left, pos.y),
                new IntVec2D(right, pos.y),
                new IntVec2D(pos.x, below),
                new IntVec2D(pos.x, above),
                // Diagonal moves
                new IntVec2D(left, below),
                new IntVec2D(left, above),
                new IntVec2D(right, above),
                new IntVec2D(right, below),
        };
    }

    /**
     * The estimated distance between two arbitrary vertices.
     */
    public float heuristic(Tile from, Tile to) {
        // straight-line distance
        return (float) Math.sqrt(distSq(from.pos.x, from.pos.y, to.pos.x, to.pos.y));
    }

    /**
     * The distance between two neighboring tiles.
     */
    public float distance(Tile from, Tile to) {
        if (from.pos.x == to.pos.x || from.pos.y == to.pos.y)
            return 1;
        return 1.4142f;
    }
    //endregion
}
