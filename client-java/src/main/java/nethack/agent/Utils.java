package nethack.agent;

import nethack.utils.NethackSurface_NavGraph.Tile;
import eu.iv4xr.framework.spatial.Vec3;

import java.util.List;

import eu.iv4xr.framework.mainConcepts.WorldEntity;

import nl.uu.cs.aplib.utils.Pair;

public class Utils {
	/**
	 * Distance in terms of path-length from the agent that owns S to the entity e.
	 * It uses adjustedFindPath to calculate the path.
	 */
	static int distTo(AgentState S, WorldEntity e) {
		var player = S.worldmodel.elements.get(S.worldmodel.agentId);
		Tile p = Utils.toTile(player.position);
		Tile target = Utils.toTile(e.position);
		var path = adjustedFindPath(S, Utils.levelId(player), p.x, p.y, Utils.levelId(e), target.x, target.y);
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
		var nav = state.multiLayerNav;
		boolean srcOriginalBlockingState = nav.isBlocking(Utils.loc3(level0, x0, y0));
		boolean destOriginalBlockingState = nav.isBlocking(Utils.loc3(level1, x1, y1));
		nav.toggleBlockingOff(Utils.loc3(level0, x0, y0));
		nav.toggleBlockingOff(Utils.loc3(level1, x1, y1));
		var path = nav.findPath(Utils.loc3(level0, x0, y0), Utils.loc3(level1, x1, y1));
		nav.setBlockingState(Utils.loc3(level0, x0, y0), srcOriginalBlockingState);
		nav.setBlockingState(Utils.loc3(level1, x1, y1), destOriginalBlockingState);
		return path;
	}
	
	public static Tile toTile(Vec3 p) {
		return new Tile((int) p.x, (int) p.y);
	}

	public static Tile toTile(int x, int y) {
		return new Tile(x, y);
	}

	static Pair<Integer, Tile> loc3(int levelId, int x, int y) {
		return new Pair<>(levelId, new Tile(x, y));
	}

	/**
	 * Check if two tiles are adjacent.
	 */
	public static boolean adjacent(Tile tile1, Tile tile2, boolean allowDiagonally) {
		if (allowDiagonally) {
			return Math.abs(tile1.x - tile2.x) <= 1 && Math.abs(tile1.y - tile2.y) <= 1 && !tile1.equals(tile2);
		}
		return (tile1.x == tile2.x && Math.abs(tile1.y - tile2.y) == 1)
				||
			   (tile1.y == tile2.y && Math.abs(tile1.x - tile2.x) == 1) ;
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
	public static float distanceBetweenEntities(AgentState S, WorldEntity e1, WorldEntity e2) {
		int e1_level = (int) e1.properties.get("level");
		int e2_level = (int) e2.properties.get("level");

		if (e1_level == e2_level) {
			var p1 = e1.position.copy();
			var p2 = e2.position.copy();
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
		var aname = S.worldmodel.agentId;
		var player = S.worldmodel.elements.get(aname);
		return distanceBetweenEntities(S, player, e);
	}

//	/**
//	 * check if the location of the entity e is reachable from the 
//	 * agent current position.
//	 */
//	public static boolean isReachable(AgentState S, WorldEntity e) {
//		var aname = S.worldmodel.agentId ;
//	    var player = S.worldmodel.elements.get(aname) ;
//	    int player_maze = (int) player.properties.get("maze") ;
//	    int e_maze = (int) e.properties.get("maze") ;
//	    
//		var t1 = toTile(player.position) ;
//		var t2 = toTile(e.position) ;
//		var path = adjustedFindPath(S, player_maze,t1.x,t1.y,e_maze,t2.x,t2.y) ;
//		return path!=null && path.size()>0 ;
//	}
}
