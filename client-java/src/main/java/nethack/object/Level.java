package nethack.object;

import eu.iv4xr.framework.spatial.IntVec2D;
import nethack.agent.navigation.NavUtils;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.Utils;

import java.util.ArrayList;
import java.util.List;

public class Level {
    public static final int HEIGHT = 21;
    public static final int WIDTH = 79;
    public Entity[][] map;
    public List<Entity> removedEntities;
    private int nr;

    public Level(int levelNr, Entity[][] entities) {
        this.nr = levelNr;
        this.map = entities;
    }

    public String id() {
        return "level" + nr;
    }

    public List<IntVec2D> mapTiles(IntVec2D playerPos) {
        List<IntVec2D> points = new ArrayList<IntVec2D>();
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                IntVec2D pos = new IntVec2D(x, y);
                Entity e = getEntity(x, y);
                if (e.type == EntityType.VOID) {
                    if (NavUtils.adjacent(playerPos, pos, true)) {
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
        return getEntity(p.x, p.y);
    }

    public Entity getEntity(int x, int y) {
        return map[y][x];
    }

    public void setEntity(IntVec2D p, Entity entity) {
        setEntity(p.x, p.y, entity);
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
                    sb.append(map[y][x].color.stringCode());
                }
                sb.append(map[y][x].symbol);
            }
            sb.append(Color.RESET.stringCode());
            if (y != map.length - 1) {
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
}
