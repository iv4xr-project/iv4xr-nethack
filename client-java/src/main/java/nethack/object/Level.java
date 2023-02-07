package nethack.object;

import agent.navigation.hpastar.Size;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.ArrayList;
import java.util.List;
import nethack.NetHackLoggers;
import nethack.enums.Color;
import nethack.enums.EntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Level {
  public static final Logger logger = LogManager.getLogger(NetHackLoggers.NetHackLogger);
  public static final Size SIZE = new Size(79, 21);
  public Entity[][] map;
  public List<IntVec2D> changedCoordinates = new ArrayList<>();
  public List<IntVec2D> visibleFloors = new ArrayList<>();

  public Level(Entity[][] entities) {
    this.map = entities;

    setVisibleFloors();
  }

  private void setVisibleFloors() {
    for (int x = 0; x < SIZE.width; x++) {
      for (int y = 0; y < SIZE.height; y++) {
        Entity e = getEntity(x, y);
        if (e.type == EntityType.FLOOR && e.color == Color.GRAY) {
          visibleFloors.add(new IntVec2D(x, y));
        }
      }
    }
  }

  public void setChangedCoordinates(Level oldLevel) {
    changedCoordinates.clear();
    // This is the first observation of the level, returns all relevant coordinates
    if (oldLevel == null) {
      setWithAllCoordinates();
      logger.debug(
          String.format(
              "Has set all coordinates of entities (%d entities)", changedCoordinates.size()));
      return;
    }

    // If it is a subsequent observation, only give coordinates of fields that changed
    for (int x = 0; x < SIZE.width; x++) {
      for (int y = 0; y < SIZE.height; y++) {
        if (!oldLevel.getEntity(x, y).equals(getEntity(x, y))) {
          changedCoordinates.add(new IntVec2D(x, y));
        }
      }
    }
    logger.debug(
        String.format(
            "Set only the changes in the level (%d entities)", changedCoordinates.size()));
  }

  // Returns coordinates of everything that is not VOID
  private void setWithAllCoordinates() {
    for (int x = 0; x < SIZE.width; x++) {
      for (int y = 0; y < SIZE.height; y++) {
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
    for (int y = 0; y < SIZE.height; y++) {
      Color currentColor = null;
      for (int x = 0; x < SIZE.width; x++) {
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
