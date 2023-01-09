package nethack.object;

import java.util.ArrayList;
import java.util.List;

import eu.iv4xr.framework.spatial.IntVec2D;

public class Level {
	public Entity[][] map;
	public List<Entity> knownEntities;
	private int nr;

	public static final int HEIGHT = 21;
	public static final int WIDTH = 79;

	public String id() {
		return "level" + nr;
	}

	public Level(int levelNr, Entity[][] entities) {
		this.nr = levelNr;
		this.map = entities;
	}

	public List<IntVec2D> visibleTiles() {
		List<IntVec2D> points = new ArrayList<IntVec2D>();
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				Entity e = getEntity(x, y);
				if (e.type == EntityType.VOID) {
					continue;
				} else if (e.color == Color.TRANSPARENT) {
					continue;
				} else {
					points.add(new IntVec2D(x, y));
				}
			}
		}
		return points;
	}
	
	public void getInvisibleTiles() {
		System.out.print("Invisible tiles:");
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				if (map[y][x].color == Color.TRANSPARENT) {
					System.out.print("(" + x + "," + y + ")");
				}
			}
		}
		System.out.println();
	}

	private List<IntVec2D> FindChanges(Level other) {
		List<IntVec2D> points = new ArrayList<IntVec2D>();
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				if (!map[y][x].equals(other.map[y][x])) {
					String s = String.format("(%s->%s x,y:%d,%d)", map[y][x].symbol, other.map[y][x].symbol, x, y);
					System.out.print(s);
					points.add(new IntVec2D(x, y));
				}
			}
		}
		System.out.println();
		return points;
	}

//	public List<IntVec2D> UpdateMap(Level newObservation) {
//		List<IntVec2D> changes = FindChanges(newObservation);
//		for (IntVec2D vec: changes) {
//			Entity entity = getEntity(vec);
//			if (entity.type == EntityType.VOID) {
//				setEntity(vec, newObservation.getEntity(vec));
//			} else if (entity.becameTransparent(newObservation.getEntity(vec))) {
//				if (entity.type == )
//			} else if (entity.becameVisible(newObservation.getEntity(vec))) {
//				
//			}
//		}
//		return changes;
//	}

	public Entity getEntity(IntVec2D p) {
		return map[p.y][p.x];
	}

	public Entity getEntity(int x, int y) {
		return map[y][x];
	}

	public void setEntity(IntVec2D p, Entity entity) {
		map[p.y][p.x] = entity;
	}

	public void setEntity(int x, int y, Entity entity) {
		map[y][x] = entity;
	}
}
