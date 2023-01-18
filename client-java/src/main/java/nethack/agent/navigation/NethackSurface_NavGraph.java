package nethack.agent.navigation;

import eu.iv4xr.framework.extensions.pathfinding.*;
import eu.iv4xr.framework.spatial.IntVec2D;
import nethack.object.Color;
import nethack.object.Level;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * The class also implements {@link Xnavigatable}, so it offers methods to do
 * pathfinding and exploration over the world.
 *
 * @author Wish
 */
public class NethackSurface_NavGraph
        implements
        Navigatable<NethackSurface_NavGraph.Tile>,
        XPathfinder<NethackSurface_NavGraph.Tile>,
        CanDealWithDynamicObstacle<NethackSurface_NavGraph.Tile> {

    // The x coordinates of this tiled-surface starts from 0 until sizeX-1
    public final static int sizeX = Level.WIDTH;
    // The y coordinates of this tiled-surface starts from 0 until sizeY-1
    public final static int sizeY = Level.HEIGHT;
    public Map<Integer, Map<Integer, NonNavigableTile>> obstacles = new HashMap<>();
    public Map<Integer, Map<Integer, Tile>> floors = new HashMap<>();
    public Map<Integer, Set<Integer>> seen = new HashMap<>();
    public Pathfinder<Tile> pathfinder = new AStar<>();
    /**
     * If true, the pathfinder will assume that the whole navgraph has been "seen",
     * so no vertex would count as unreacahble because it is still unseen. This
     * essentially turns off memory-based path finding. The default of this flag is
     * false.
     */
    boolean perfect_memory_pathfinding = false;
    Set<Tile> frontierCandidates = new HashSet<>();

    /**
     * Add a non-navigable tile (obstacle).
     */
    public void addObstacle(Tile o) {
        if (!(o instanceof NonNavigableTile))
            throw new IllegalArgumentException();

        Map<Integer, NonNavigableTile> xMap = obstacles.computeIfAbsent(o.pos.x, k -> new HashMap<>());
        xMap.put(o.pos.y, (NonNavigableTile) o);

        // Remove floor
        Map<Integer, Tile> fMap = floors.get(o.pos.x);
        if (fMap != null) {
            fMap.remove(o.pos.y);
        }
    }

    /**
     * Remove a non-navigable tile (obstacle).
     */
    public void removeObstacle(Tile o) {
        Map<Integer, NonNavigableTile> xMap = obstacles.get(o.pos.x);
        if (xMap != null) {
            xMap.remove(o.pos.y);
        }

        Map<Integer, Tile> fMap = floors.computeIfAbsent(o.pos.x, k -> new HashMap<>());
        fMap.put(o.pos.y, o);
    }

    public void markAsSeen(Tile p) {
        Set<Integer> ys = seen.computeIfAbsent(p.pos.x, k -> new HashSet<>());
        ys.add(p.pos.y);
        frontierCandidates.add(p);
    }

    public void markAsSeen(List<Tile> newlyseen) {
        for (Tile p : newlyseen) {
            markAsSeen(p);
        }
    }

    public boolean hasbeenSeen(int x, int y) {
        Set<Integer> ys = seen.get(x);
        return ys != null && ys.contains(y);
    }

    public boolean hasbeenSeen(Tile tile) {
        return hasbeenSeen(tile.pos.x, tile.pos.y);
    }

    /**
     * The tile is blocking (true) if it is a wall or a closed door. Else it is
     * non-blocking (false).
     */
    private boolean isBlocking(IntVec2D pos) {
        return isBlocking(new Tile(pos));
    }

    public boolean isBlocking(Tile tile) {
        if (isWall(tile.pos))
            return true;
        if (isDoor(tile.pos)) {
            NonNavigableTile o = getObstacle(tile.pos);
            Door door = (Door) o;
            return !door.isOpen;
        }
        return false;
    }

    public boolean isDiagonalDoorMove(Tile tile, int x, int y) {
        if (tile.pos.x == x || tile.pos.y == y) {
            return false;
        }
        return isDoor(tile.pos) || isDoor(new IntVec2D(x, y));
    }

    @Override
    /**
     * Set the blocking state of this tile, if it is a Door, to the given
     * blocking-state (true means blocking, false non-blocking).
     *
     * If the tile is not a Door, this method has no effect.
     */
    public void setBlockingState(Tile tile, boolean isBlocking) {
        if (isDoor(tile.pos)) {
            NonNavigableTile o = getObstacle(tile.pos);
            Door door = (Door) o;
            door.isOpen = !isBlocking;
        }
    }

    public boolean isDoor(IntVec2D pos) {
        NonNavigableTile o = getObstacle(pos);
        if (o == null)
            return false;
        return o instanceof Door;
    }

    public boolean isFloor(IntVec2D pos) {
        Tile o = getFloor(pos);
        if (o == null)
            return false;
        return o instanceof Floor;
    }

    public boolean isDoorway(IntVec2D pos) {
        Tile o = getFloor(pos);
        if (o == null)
            return false;
        return o instanceof Doorway;
    }

    public boolean isWall(IntVec2D pos) {
        NonNavigableTile o = getObstacle(pos);
        if (o == null)
            return false;
        return o instanceof Wall;
    }

    public boolean isCorridor(IntVec2D pos) {
        Tile o = getFloor(pos);
        if (o == null)
            return false;
        return o instanceof Corridor;
    }

    /**
     * When true then the pathfinder will consider all nodes in the graph to have
     * been seen.
     */
    public boolean usingPerfectMemoryPathfinding() {
        return perfect_memory_pathfinding;
    }

    /**
     * When true then the pathfinder will consider all nodes in the graph to have
     * been seen.
     */
    public void setPerfectMemoryPathfinding(Boolean flag) {
        perfect_memory_pathfinding = flag;
    }

    /**
     * Mark all vertices as "unseen".
     */
    public void wipeOutMemory() {
        seen.clear();
        frontierCandidates.clear();
    }

    public static List<IntVec2D> physicalNeighbourCoordinates(IntVec2D pos) {
        int left = pos.x - 1;
        int right = pos.x + 1;
        int below = pos.y - 1;
        int above = pos.y + 1;

        List<IntVec2D> candidates = new ArrayList<>();
        if (left >= 0)
            candidates.add(new IntVec2D(left, pos.y));
        if (right < sizeX)
            candidates.add(new IntVec2D(right, pos.y));
        if (below >= 0)
            candidates.add(new IntVec2D(pos.x, below));
        if (above < sizeY)
            candidates.add(new IntVec2D(pos.x, above));
        // Diagonal moves
        if (left >= 0 && below >= 0)
            candidates.add(new IntVec2D(left, below));
        if (left >= 0 && above < sizeY)
            candidates.add(new IntVec2D(left, above));
        if (right < sizeX && above < sizeY)
            candidates.add(new IntVec2D(right, above));
        if (right < sizeX && below >= 0)
            candidates.add(new IntVec2D(right, below));

        return candidates;
    }

    /**
     * Return the neighbors of a tile. A tile u is a neighbor of a tile t if u is
     * adjacent to t. This method does not consider whether u has been seen or not,
     * nor whether u is navigable.
     */
    public List<Tile> physicalNeighbours(IntVec2D pos) {
        List<IntVec2D> neighbourCoordinates = physicalNeighbourCoordinates(pos);
        List<Tile> candidates = neighbourCoordinates.stream().map(Tile::new).collect(Collectors.toList());

//        candidates = candidates.stream()
//            // .filter(c -> ! isBlocked(c.x,c.y))
//            .collect(Collectors.toList());
//        System.out.println("&&&& " + candidates.size());

        return candidates;
    }

    /**
     * Return the neighbors of a tile. A tile u is a neighbor of a tile t if u is
     * adjacent to t, and moreover u is navigable (e.g. it is not a wall or a closed
     * door). If the flag diagonalMovementPossible is true, then tiles that are
     * diagonally touching t are also considered neighbors.
     *
     * <p>
     * Only neighbors that have been seen before will be included.
     */
    public List<Tile> neighbours_(int x, int y) {
        List<Tile> candidates = physicalNeighbours(new IntVec2D(x, y));
        // System.out.println("=== (" + x + "," + y + ") -> " + candidates.size()) ;

        // Cant navigate to block and cannot diagonally move in/out a door
        candidates = candidates.stream().filter(c -> !isBlocking(c) && !isDiagonalDoorMove(c, x, y)).collect(Collectors.toList());

        if (!perfect_memory_pathfinding) {
            candidates = candidates.stream().filter(c -> hasbeenSeen(c.pos.x, c.pos.y)).collect(Collectors.toList());
        }
        // System.out.println("=== " + candidates.size() + ":" + candidates) ;
        return candidates;
    }

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
        return neighbours_(t.pos.x, t.pos.y);
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

    public List<Tile> findPath(Tile from, Tile to) {
        return pathfinder.findPath(this, from, to);
    }

    public List<Tile> findPath(int fromX, int fromY, int toX, int toY) {
        return findPath(new Tile(fromX, fromY), new Tile(toX, toY));
    }

    /**
     * This returns the set of frontier-tiles. A tile is a frontier tile if it is a
     * seen/explored tile, and it has at least one unexplored and unblocked neighbor.
     * Note that under this definition a frontier does not have to be reachable. You
     * can use {@link findPath} to check which frontiers are reachable.
     */
    public List<Tile> getFrontier() {
        List<Tile> frontiers = new LinkedList<>();
        List<Tile> cannotBeFrontier = new LinkedList<>();
        for (Tile t : frontierCandidates) {
            List<Tile> pneighbors = physicalNeighbours(t.pos);
            boolean isFrontier = false;
            for (Tile n : pneighbors) {
                if (!hasbeenSeen(n.pos.x, n.pos.y)) {
                    frontiers.add(t);
                    isFrontier = true;
                    break;
                }
            }
            if (!isFrontier) {
                cannotBeFrontier.add(t);
            }
        }
        // remove tiles that are obviously not frontiers:
        frontierCandidates.removeAll(cannotBeFrontier);
        // System.out.println(">>> Sparse2D.getFrontier() is called") ;
        return frontiers;
    }

    float distSq(int x1, int y1, int x2, int y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    public List<Tile> explore(Tile startingLocation, Tile heuristicLocation) {
        return explore(startingLocation.pos.x, startingLocation.pos.y, heuristicLocation.pos.x, heuristicLocation.pos.y);
    }

    public List<Tile> explore(int x, int y, int heuristicX, int heuristicY) {

        List<Tile> frontiers = getFrontier();

        if (frontiers.isEmpty())
            return null;
        // sort the frontiers ascendingly, by their geometric distance to (x,y):
        frontiers.sort((p1, p2) -> Float.compare(distSq(p1.pos.x, p1.pos.y, x, y), distSq(p2.pos.x, p2.pos.y, heuristicX, heuristicY)));

        // System.out.println(">>> #frontiers:" + frontiers.size()) ;

        for (Tile front : frontiers) {
            // System.out.println(">>> (" + x + "," + y + ") --> (" + front.x + "," +
            // front.y + ")" ) ;
            List<Tile> path = findPath(x, y, front.pos.x, front.pos.y);
            // System.out.println("==== path " + path) ;
            // System.out.println("frontier path " + path +" frontier vertices: "+
            // front.fst);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    public boolean hasTile(IntVec2D pos) { return getAnyTile(pos) != null; }

    private Tile getAnyTile(IntVec2D pos) {
        Tile t = getFloor(pos);
        if (t != null) {
            return t;
        }

        return getObstacle(pos);
    }

    private NonNavigableTile getObstacle(IntVec2D pos) {
        var xmap = obstacles.get(pos.x);
        if (xmap != null) {
            return xmap.get(pos.y);
        }
        return null;
    }

    private Tile getFloor(IntVec2D pos) {
        var fmap = floors.get(pos.x);
        if (fmap != null) {
            return fmap.get(pos.y);
        }
        return null;
    }


    public List<IntVec2D> VisibleCoordinates(IntVec2D agentPosition, Level level) {
        // First reset visibility
        for (var xmap: obstacles.values()) {
            for (Tile t: xmap.values()) {
                t.visible = false;
            }
        }

        for (var fmap: floors.values()) {
            for (Tile t: fmap.values()) {
                t.visible = false;
            }
        }

        // Perform BFS on the graph
        HashSet<IntVec2D> visibleCoordinates = new HashSet<>();
        Queue<IntVec2D> queue = new LinkedList<>();
        queue.add(agentPosition);

        // While there are coordinates left to be explored
        while (queue.size() > 0) {
            IntVec2D nextPos = queue.remove();
            // Already processed
            if (visibleCoordinates.contains(nextPos)) {
                continue;
            }

            Tile t = getAnyTile(nextPos);
            // Void
            if (t == null) {
                continue;
            }

            // Definitely not visible
            if (level.getEntity(nextPos).color == Color.TRANSPARENT) {
                continue;
            }

            // Get the neighbours
            List<IntVec2D> neighbours = physicalNeighbourCoordinates(nextPos);
            if (isDoorway(nextPos)) {
                // Does not have a lit floor tile next to it, so we assume we cannot see it
                if (neighbours.stream().noneMatch(coord -> isFloor(coord) && level.getEntity(coord).color != Color.TRANSPARENT)) {
                    continue;
                }
            }

            // Current tile is visible
            t.visible = true;
            visibleCoordinates.add(nextPos);

            // Wall or closed door blocks from seeing further
            if (isBlocking(new Tile(nextPos))) {
                continue;
            } // Open doors  except if the agent is in that position
            else if (!isCorridor(nextPos) && !isDoorway(nextPos) || nextPos == agentPosition) {
                queue.addAll(neighbours);
            } // Only add tiles for further exploration if they
            else {
                Stream<IntVec2D> adjacentFloors = neighbours.stream().filter(coord -> !isDoorway(coord) && !isCorridor(coord) && !isBlocking(coord));
                queue.addAll(adjacentFloors.collect(Collectors.toList()));
            }
        }

        return new ArrayList<>(visibleCoordinates);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // Add row by row to the stringbuilder
        for (int y = 0; y < sizeY; y++) {
            Color currentColor = Color.RESET;
            for (int x = 0; x < sizeX; x++) {
                // Get tile, if it doens't know the type it is not know or void.
                Tile t = getAnyTile(new IntVec2D(x, y));
                boolean wasSeen = hasbeenSeen(x, y);
                if (t == null) {
                    sb.append(wasSeen ? '?' : ' ');
                    continue;
                }

                // Check for color swap
                Color desiredColor = t.visible ? Color.GREEN_BRIGHT : Color.RESET;
                if (!desiredColor.equals(currentColor)) {
                    sb.append(desiredColor.stringCode());
                    currentColor = desiredColor;
                };

                sb.append(t.toChar(wasSeen));
            }

            // Reset color
            sb.append(Color.RESET.stringCode());

            // Don't add line after last row
            if (y != sizeY - 1) {
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    public static class Tile {
        public IntVec2D pos;
        public boolean visible = false;

        public Tile(IntVec2D pos) {
            this.pos = pos;
        }

        public Tile(int x, int y) {
            this.pos = new IntVec2D(x, y);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Tile) {
                Tile t = (Tile) o;
                return pos.x == t.pos.x && pos.y == t.pos.y;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos);
        }

        @Override
        public String toString() {
            return "(" + pos.x + "," + pos.y + ")";
        }

        public char toChar(boolean wasSeen) { return '?'; }
    }

    public static class Floor extends Tile {
        public Floor(IntVec2D pos) { super(pos); }

        @Override
        public char toChar(boolean wasSeen) { return '·'; }
    }

    public static class Doorway extends Tile {
        public Doorway(IntVec2D pos) { super(pos); }

        @Override
        public char toChar(boolean wasSeen) { return '·'; }
    }

    public static class Corridor extends Tile {
        public Corridor(IntVec2D pos) { super(pos); }

        @Override
        public char toChar(boolean wasSeen) { return '-'; }
    }

    public static class NonNavigableTile extends Tile {
        public NonNavigableTile(IntVec2D pos) { super(pos); }
    }

    public static class Wall extends NonNavigableTile {
        public Wall(IntVec2D pos) { super(pos); }

        @Override
        public char toChar(boolean wasSeen) { return wasSeen ? 'W' : 'w'; }
    }

    public static class Door extends NonNavigableTile {
        boolean isOpen = false;

        public Door(IntVec2D pos) { super(pos); }

        public Door(IntVec2D pos, boolean isOpen) {
            super(pos);
            this.isOpen = isOpen;
        }

        @Override
        public char toChar(boolean wasSeen) {
            if (isOpen) {
                return wasSeen ? 'O' : 'o';
            } else {
                return wasSeen ? 'X' : 'x';
            }
        }
    }
}
