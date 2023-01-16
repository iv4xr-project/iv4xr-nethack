package nethack.agent.navigation;

import eu.iv4xr.framework.extensions.pathfinding.LayeredAreasNavigation;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;
import nethack.agent.AgentState;
import nethack.object.Command;
import nethack.utils.NethackSurface_NavGraph;
import nethack.utils.NethackSurface_NavGraph.Tile;
import nl.uu.cs.aplib.utils.Pair;

import java.util.List;

public class NavUtils {
    /**
     * Distance in terms of path-length from the agent that owns S to the entity e.
     * It uses adjustedFindPath to calculate the path.
     */
    static int distTo(AgentState S, WorldEntity e) {
        WorldEntity player = S.worldmodel.elements.get(S.worldmodel.agentId);
        Tile p = toTile(player.position);
        Tile target = toTile(e.position);
        List<Pair<Integer, Tile>> path = adjustedFindPath(S, levelId(player), p.x, p.y, levelId(e), target.x, target.y);
        if (path == null)
            return Integer.MAX_VALUE;
        return path.size() - 1;
    }

    /**
     * Calculate a path from (x0,y0) in maze-0 to (x1,y1) in maze-1. The method will
     * pretend that the source (x0,y0) and destination (x1,y1) are non-blocking
     * (even if they are, e.g. if one of them is an occupied tile).
     */
    public static List<Pair<Integer, Tile>> adjustedFindPath(AgentState state, int level0, int x0, int y0, int level1,
                                                             int x1, int y1) {
        LayeredAreasNavigation<Tile, NethackSurface_NavGraph> nav = state.multiLayerNav;
        Pair<Integer, Tile> oldLocation = loc3(level0, x0, y0);
        Pair<Integer, Tile> newLocation = loc3(level1, x1, y1);
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

    static Pair<Integer, Tile> loc3(int levelId, int x, int y) {
        return new Pair<>(levelId, toTile(x, y));
    }

    public static boolean adjacent(Vec3 pos1, Vec3 pos2, boolean allowDiagonally) {
        return adjacent(toTile(pos1), toTile(pos2), allowDiagonally);
    }

    // Check if two tiles are adjacent.
    public static boolean adjacent(Tile tile1, Tile tile2, boolean allowDiagonally) {
        int dx = Math.abs(tile1.x - tile2.x);
        int dy = Math.abs(tile1.y - tile2.y);

        // Further than 1 away or same tile
        if (dx > 1 || dy > 1 || (dx == 0 && dy == 0)) {
            return false;
        }
        return allowDiagonally || dx == 0 || dy == 0;
    }

    public static int manhattanDist(Tile t1, Tile t2) {
        return Math.abs(t1.x - t2.x) + Math.abs(t1.y - t2.y);
    }

    public static int levelId(WorldEntity e) {
        return (int) e.properties.get("level");
    }

    /**
     * Give the straight-line distance-square between two entities, if they are in
     * the same maze; else the distance is the difference between mazeIds times some
     * large multiplier (1000000).
     */
    public static float distanceBetweenEntities(WorldEntity e1, WorldEntity e2) {
        int e1_level = levelId(e1);
        int e2_level = levelId(e2);

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
        String agentName = S.worldmodel.agentId;
        WorldEntity player = S.worldmodel.elements.get(agentName);
        return distanceBetweenEntities(player, e);
    }

    public static WorldModel moveTo(AgentState state, Tile targetTile) {
        Command command = stepToCommand(state, targetTile);
        return state.env().action(command);
    }

    public static WorldModel moveTo(AgentState state, Vec3 targetPosition) {
        return moveTo(state, toTile(targetPosition));
    }

    public static Command stepToCommand(AgentState state, Tile targetTile) {
        Tile t0 = toTile(state.worldmodel.position);
        if (!adjacent(t0, targetTile, true)) {
            throw new IllegalArgumentException("");
        }

        if (targetTile.y > t0.y) {
            if (targetTile.x > t0.x) {
                return Command.DIRECTION_SE;
            } else if (targetTile.x < t0.x) {
                return Command.DIRECTION_SW;
            } else {
                return Command.DIRECTION_S;
            }
        } else if (targetTile.y < t0.y) {
            if (targetTile.x > t0.x) {
                return Command.DIRECTION_NE;
            } else if (targetTile.x < t0.x) {
                return Command.DIRECTION_NW;
            } else {
                return Command.DIRECTION_N;
            }
        } else if (targetTile.x > t0.x) {
            return Command.DIRECTION_E;
        } else {
            return Command.DIRECTION_W;
        }
    }

//	/**
//	 * check if the location of the entity e is reachable from the 
//	 * agent current position.
//	 */
//	public static boolean isReachable(AgentState S, WorldEntity e) {
//		var agentName = S.worldmodel.agentId ;
//	    var player = S.worldmodel.elements.get(agentName) ;
//	    int player_maze = (int) player.properties.get("maze") ;
//	    int e_maze = (int) e.properties.get("maze") ;
//	    
//		var t1 = toTile(player.position) ;
//		var t2 = toTile(e.position) ;
//		var path = adjustedFindPath(S, player_maze,t1.x,t1.y,e_maze,t2.x,t2.y) ;
//		return path!=null && path.size()>0 ;
//	}
}
