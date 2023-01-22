package agent.navigation;

import agent.AgentLoggers;
import agent.AgentState;
import agent.navigation.surface.Tile;
import eu.iv4xr.framework.extensions.pathfinding.LayeredAreasNavigation;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import nethack.enums.Command;
import nethack.object.Player;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class NavUtils {
    static final Logger logger = LogManager.getLogger(AgentLoggers.NavLogger);

    /**
     * Distance in terms of path-length from the agent that owns S to the entity e.
     * It uses adjustedFindPath to calculate the path.
     */
    static int distTo(AgentState S, WorldEntity e) {
        List<Pair<Integer, Tile>> path = adjustedFindPath(S, NavUtils.loc3(S.worldmodel.position), NavUtils.loc3(e.position));
        if (path == null) {
            return Integer.MAX_VALUE;
        }
        return path.size() - 1;
    }

    /**
     * Calculate a path from (x0,y0) in maze-0 to (x1,y1) in maze-1. The method will
     * pretend that the source (x0,y0) and destination (x1,y1) are non-blocking
     * (even if they are, e.g. if one of them is an occupied tile).
     */
    public static List<Pair<Integer, Tile>> adjustedFindPath(AgentState state, int level0, IntVec2D pos0, int level1, IntVec2D pos1) {
        return adjustedFindPath(state, loc3(level0, pos0), loc3(level1, pos1));
    }

    public static List<Pair<Integer, Tile>> adjustedFindPath(AgentState state, Pair<Integer, Tile> oldLocation, Pair<Integer, Tile> newLocation) {
        LayeredAreasNavigation<Tile, NetHackSurface> nav = state.multiLayerNav;
        boolean srcOriginalBlockingState = nav.isBlocking(oldLocation);
        boolean destOriginalBlockingState = nav.isBlocking(newLocation);
        nav.toggleBlockingOff(oldLocation);
        nav.toggleBlockingOff(newLocation);
        List<Pair<Integer, Tile>> path = nav.findPath(oldLocation, newLocation);
        nav.setBlockingState(oldLocation, srcOriginalBlockingState);
        nav.setBlockingState(newLocation, destOriginalBlockingState);
        return path;
    }

    public static Tile toTile(Vec3 p) {
        return toTile((int) p.x, (int) p.y);
    }

    public static Tile toTile(int x, int y) {
        return new Tile(x, y);
    }

    public static Tile toTile(IntVec2D pos) {
        return new Tile(pos);
    }

    public static IntVec2D loc2(int x, int y) {
        return new IntVec2D(x, y);
    }

    public static IntVec2D loc2(Vec3 pos) {
        return new IntVec2D((int) pos.x, (int) pos.y);
    }

    static Pair<Integer, Tile> loc3(int levelNr, int x, int y) {
        return loc3(levelNr, loc2(x, y));
    }

    static Pair<Integer, Tile> loc3(int levelNr, IntVec2D pos) {
        return new Pair<>(levelNr, toTile(pos));
    }

    static Pair<Integer, Tile> loc3(Vec3 pos) {
        return new Pair<>((int) pos.z, toTile(pos));
    }

    public static boolean adjacent(Vec3 vec1, Vec3 vec2, boolean allowDiagonally) {
        return adjacent(toTile(vec1), toTile(vec2), allowDiagonally);
    }

    public static boolean adjacent(IntVec2D pos1, IntVec2D pos2, boolean allowDiagonally) {
        return adjacent(pos1.x, pos1.y, pos2.x, pos2.y, allowDiagonally);
    }

    // Check if two tiles are adjacent.
    public static boolean adjacent(Tile tile1, Tile tile2, boolean allowDiagonally) {
        return adjacent(tile1.pos.x, tile1.pos.y, tile2.pos.x, tile2.pos.y, allowDiagonally);
    }

    private static boolean adjacent(int x0, int y0, int x1, int y1, boolean allowDiagonally) {
        int dx = Math.abs(x0 - x1);
        int dy = Math.abs(y0 - y1);

        // Further than 1 away or same tile
        if (dx > 1 || dy > 1 || (dx == 0 && dy == 0)) {
            return false;
        }
        return allowDiagonally || dx == 0 || dy == 0;
    }

    public static int manhattanDist(Tile t1, Tile t2) {
        return Math.abs(t1.pos.x - t2.pos.x) + Math.abs(t1.pos.y - t2.pos.y);
    }

    public static int levelNr(WorldEntity e) {
        return (int) e.properties.get("level");
    }

    public static int levelNr(Vec3 pos) {
        return (int) pos.z;
    }

    /**
     * Give the straight-line distance-square between two entities, if they are in
     * the same maze; else the distance is the difference between mazeIds times some
     * large multiplier (1000000).
     */
    public static float distanceBetweenEntities(WorldEntity e1, WorldEntity e2) {
        int e1_level = levelNr(e1);
        int e2_level = levelNr(e2);

        if (e1_level == e2_level) {
            Vec3 p1 = e1.position.copy();
            Vec3 p2 = e2.position.copy();
            p1.z = 0;
            p2.z = 0;
            return Vec3.distSq(p1, p2);
        }
        return Math.abs(e1_level - e2_level) * 1000000;
    }

    /**
     * Give the straight-line distance-square between the agent that owns the given
     * state and the given entity e, if they are in the same maze; else the distance
     * is the difference between their mazeIds times some large multiplier
     * (1000000).
     */
    public static float distanceToAgent(AgentState S, WorldEntity e) {
        return distanceBetweenEntities(S.worldmodel.elements.get(Player.ID), e);
    }

    public static WorldModel moveTo(AgentState state, Tile targetTile) {
        Command command = stepToCommand(state, targetTile);
        return state.env().action(command);
    }

    public static WorldModel moveTo(AgentState state, Vec3 targetPosition) {
        return moveTo(state, toTile(targetPosition));
    }

    public static Command stepToCommand(AgentState state, Tile targetTile) {
        IntVec2D agentPos = NavUtils.loc2(state.worldmodel.position);
        if (!adjacent(agentPos, targetTile.pos, true)) {
            throw new IllegalArgumentException(String.format("Step from %s to %s is illegal", agentPos, targetTile));
        }

        if (targetTile.pos.y > agentPos.y) {
            if (targetTile.pos.x > agentPos.x) {
                return Command.DIRECTION_SE;
            } else if (targetTile.pos.x < agentPos.x) {
                return Command.DIRECTION_SW;
            } else {
                return Command.DIRECTION_S;
            }
        } else if (targetTile.pos.y < agentPos.y) {
            if (targetTile.pos.x > agentPos.x) {
                return Command.DIRECTION_NE;
            } else if (targetTile.pos.x < agentPos.x) {
                return Command.DIRECTION_NW;
            } else {
                return Command.DIRECTION_N;
            }
        } else if (targetTile.pos.x > agentPos.x) {
            return Command.DIRECTION_E;
        } else {
            return Command.DIRECTION_W;
        }
    }

    public static Tile nextTile(List<Pair<Integer, Tile>> path) {
        if (path == null) {
            logger.debug("Path not found");
            return null;
        } else if (path.size() == 0) {
            logger.debug("Already on location, path is length 0");
            return null;
        } else {
            // The first element is the src itself, so we need to pick the next one:
            return path.get(1).snd;
        }
    }

    public static IntVec2D[] neighbourCoordinates(IntVec2D pos) {
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
   * check if the location of the entity e is reachable from the
   * agent current position.
   */
  public static boolean isReachable(AgentState S, WorldEntity e) {
    var agentName = S.worldmodel.agentId ;
    var player = S.worldmodel.elements.get(agentName) ;
    int player_maze = (int) player.properties.get("maze") ;
    int e_maze = (int) e.properties.get("maze") ;

    var t1 = toTile(player.position) ;
    var t2 = toTile(e.position) ;
    var path = adjustedFindPath(S, player_maze,t1.x,t1.y,e_maze,t2.x,t2.y) ;
    return path!=null && path.size()>0 ;
  }
}
