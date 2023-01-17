package nethack.object;

import eu.iv4xr.framework.spatial.IntVec2D;
import nethack.agent.navigation.NavUtils;
import nl.uu.cs.aplib.exampleUsages.miniDungeon.testAgent.Utils;

import java.util.ArrayList;
import java.util.List;

public class Level {
    public static final int HEIGHT = 21, WIDTH = 79;
    public Entity[][] map;
    public List<IntVec2D> changedCoordinates = new ArrayList<>();
    private int nr;

    public Level(int levelNr, Entity[][] entities) {
        this.nr = levelNr;
        this.map = entities;
    }

    public String id() {
        return "level" + nr;
    }

    public void setChangedCoordinates(Level oldLevel) {
        changedCoordinates.clear();
        // This is the first observation of the level, returns all relevant coordinates
        if (oldLevel == null) {
            System.out.println("Set all coordinates of entities");
            setWithAllCoordinates();
            return;
        }

        System.out.println("Set only the changes");
        // If it is a subsequent observation, only give coordinates of fields that changed
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                if (!oldLevel.getEntity(x, y).equals(getEntity(x, y))) {
                    changedCoordinates.add(new IntVec2D(x, y));
                }
            }
        }
    }

    // Returns coordinates of everything that is not VOID
    private void setWithAllCoordinates() {
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                Entity e = getEntity(x, y);
                if (e.type != EntityType.VOID) {
                    changedCoordinates.add(new IntVec2D(x, y));
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
