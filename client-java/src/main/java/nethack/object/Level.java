package nethack.object;

import java.util.ArrayList;
import java.util.List;

import eu.iv4xr.framework.spatial.IntVec2D;

public class Level {
	public Entity[][] map;
	public List<Entity> removedEntities;
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

	public List<IntVec2D> visibleTiles(IntVec2D playerPos) {
		List<IntVec2D> points = new ArrayList<IntVec2D>();
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				Entity e = getEntity(x, y);
				if (e.type == EntityType.VOID) {
					if (Math.abs(playerPos.x - x) <= 1 && Math.abs(playerPos.y - y) <= 1
							&& (playerPos.x != x || playerPos.y != y)) {
						points.add(new IntVec2D(x, y));
					}
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

	public void setRemovedEntities(Level oldLevel) {
		removedEntities = new ArrayList<Entity>();
		
		if (oldLevel == null) {
			return;
		}
		
		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				if (!getEntity(x, y).equals(oldLevel.getEntity(x, y))) {
//					String s = String.format("(%s->%s x,y:%d,%d)", map[y][x].symbol, oldLevel.map[y][x].symbol, x, y);
//					System.out.print(s);
					removedEntities.add(oldLevel.getEntity(x, y));
				}
			}
		}
	}

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
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int y = 0; y < HEIGHT; y++) {
			Color currentColor = null;
			for (int x = 0; x < WIDTH; x++) {
				// Color changed so add it to the line
				if (currentColor != map[y][x].color) {
					currentColor = map[y][x].color;
					sb.append("\033[" + map[y][x].color.colorCode + "m");
				}
				sb.append(map[y][x].symbol);
			}
			sb.append("\033[m");
			if (y != map.length - 1) {
				sb.append(System.lineSeparator());
			}
		}
		return sb.toString();
	}
}
