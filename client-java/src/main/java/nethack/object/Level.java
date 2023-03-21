package nethack.object;

import agent.navigation.hpastar.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import nethack.enums.Color;
import nethack.enums.EntityType;
import util.ColoredStringBuilder;
import util.CustomVec2D;
import util.Loggers;

public class Level {
  public static final Size SIZE = new Size(79, 21);
  public final Entity[][] map;
  public final List<CustomVec2D> changedCoordinates = new ArrayList<>();
  public Set<CustomVec2D> visibleFloors = new HashSet<>();

  public Level(Entity[][] entities) {
    this.map = entities;

    // All visible floors are candidates for visibility checks
    setVisibleFloors();
  }

  private void setVisibleFloors() {
    for (int x = 0; x < SIZE.width; x++) {
      for (int y = 0; y < SIZE.height; y++) {
        Entity e = getEntity(x, y);
        if (e.type == EntityType.FLOOR && e.color == Color.GRAY) {
          visibleFloors.add(new CustomVec2D(x, y));
        }
      }
    }
  }

  public void setChangedCoordinates(Level oldLevel) {
    changedCoordinates.clear();
    // This is the first observation of the level, returns all relevant coordinates
    if (oldLevel == null) {
      setWithAllCoordinates();
      Loggers.NetHackLogger.debug(
          "Has set all coordinates of entities (%d entities)", changedCoordinates.size());
      return;
    }

    // If it is a subsequent observation, only give coordinates of fields that changed
    for (int x = 0; x < SIZE.width; x++) {
      for (int y = 0; y < SIZE.height; y++) {
        if (!oldLevel.getEntity(x, y).equals(getEntity(x, y))) {
          changedCoordinates.add(new CustomVec2D(x, y));
        }
      }
    }
    Loggers.NetHackLogger.debug(
        String.format(
            "Set only the changes in the level (%d entities)", changedCoordinates.size()));
  }

  // Returns coordinates of everything that is not VOID
  private void setWithAllCoordinates() {
    for (int x = 0; x < SIZE.width; x++) {
      for (int y = 0; y < SIZE.height; y++) {
        Entity e = getEntity(x, y);
        if (e.type != EntityType.VOID) {
          changedCoordinates.add(new CustomVec2D(x, y));
        }
      }
    }
  }

  public Entity getEntity(CustomVec2D p) {
    return getEntity(p.x, p.y);
  }

  public Entity getEntity(int x, int y) {
    return map[y][x];
  }

  public void setEntity(CustomVec2D p, Entity entity) {
    setEntity(p.x, p.y, entity);
  }

  public void setEntity(int x, int y, Entity entity) {
    map[y][x] = entity;
  }

  @Override
  public String toString() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    for (int y = 0; y < SIZE.height; y++) {
      for (int x = 0; x < SIZE.width; x++) {
        csb.setColor(map[y][x].color);
        csb.append(map[y][x].symbol);
      }
      csb.resetColor();
      if (y != map.length - 1) {
        csb.newLine();
      }
    }
    return csb.toString();
  }
}
