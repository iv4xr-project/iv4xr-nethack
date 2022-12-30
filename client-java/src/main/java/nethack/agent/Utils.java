package nethack.agent;

import eu.iv4xr.framework.extensions.pathfinding.Sparse2DTiledSurface_NavGraph.Tile;
import eu.iv4xr.framework.spatial.Vec3;

public class Utils {
	public static Tile toTile(Vec3 p) {
		return new Tile((int)p.x, (int)p.z);
	}
	
	public static Tile toTile(int x, int y) {
		return new Tile(x, y);
	}
}