package nethack.world;

import agent.navigation.hpastar.Size;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Tile;
import java.util.*;
import nethack.enums.Color;
import nethack.enums.EntityType;
import nethack.object.Entity;
import nethack.world.tiles.Door;
import nethack.world.tiles.Floor;
import nethack.world.tiles.Viewable;
import util.ColoredStringBuilder;
import util.CustomVec2D;
import util.Loggers;

public class Level {
  public static final Size SIZE = new Size(79, 21);
  public final Entity[][] map = new Entity[SIZE.height][SIZE.width];
  public Surface surface = new Surface();

  public final List<Tile> changedTiles = new ArrayList<>();
  public final List<CustomVec2D> changedEntities = new ArrayList<>();
  public Set<CustomVec2D> visibleCoordinates = new HashSet<>();

  public Level(CustomVec2D playerPos, Entity[][] entities, Tile[][] tiles) {
    updateLevel(playerPos, entities, tiles);
  }

  public void updateLevel(CustomVec2D playerPos, Entity[][] newEntities, Tile[][] newTiles) {
    updateEntities(newEntities);
    updateTiles(newTiles);
    updateVisibility(playerPos);
  }

  private void updateEntities(Entity[][] entities) {
    changedEntities.clear();

    // If it is a subsequent observation, only give coordinates of fields that changed
    for (int x = 0; x < SIZE.width; x++) {
      for (int y = 0; y < SIZE.height; y++) {
        CustomVec2D pos = new CustomVec2D(x, y);
        if (!Objects.equals(getEntity(pos), entities[y][x])) {
          setEntity(pos, entities[y][x]);
          changedEntities.add(pos);
        }
      }
    }

    Loggers.NetHackLogger.debug("%d entity(s) changes in the level", changedEntities.size());
  }

  private void updateTiles(Tile[][] tiles) {
    changedTiles.clear();

    // If it is a subsequent observation, only give coordinates of fields that changed
    for (int x = 0; x < SIZE.width; x++) {
      for (int y = 0; y < SIZE.height; y++) {
        CustomVec2D pos = new CustomVec2D(x, y);
        if (!Objects.equals(surface.getTile(pos), tiles[y][x])) {
          changedTiles.add(tiles[y][x]);
        }
      }
    }

    surface.updateTiles(changedTiles);

    Loggers.NetHackLogger.debug("%d tile(s) changes in the level", changedEntities.size());
  }

  private void updateVisibility(CustomVec2D agentPosition) {
    resetVisibility();

    Set<CustomVec2D> visibleFloors = new HashSet<>();
    for (int x = 0; x < SIZE.width; x++) {
      for (int y = 0; y < SIZE.height; y++) {
        Entity e = getEntity(x, y);
        if (e.type == EntityType.FLOOR && e.color == Color.GRAY) {
          visibleFloors.add(new CustomVec2D(x, y));
        }
      }
    }

    // Perform BFS on the graph, initiate the queue with the agent position and all the lit floor
    // tiles
    List<CustomVec2D> agentNeighbours =
        NavUtils.neighbourCoordinates(agentPosition, Level.SIZE, true);
    for (CustomVec2D neighbour : agentNeighbours) {
      Tile neighbourTile = surface.getTile(neighbour);
      if (!(neighbourTile instanceof Viewable)) {
        continue;
      }
      ((Viewable) neighbourTile).setVisibility(true);
    }

    visibleCoordinates = new HashSet<>(agentNeighbours);
    HashSet<CustomVec2D> processedCoordinates = new HashSet<>();
    Queue<CustomVec2D> queue = new LinkedList<>(visibleFloors);

    processedCoordinates.add(agentPosition);
    queue.addAll(NavUtils.neighbourCoordinates(agentPosition, Level.SIZE, true));

    // While there are coordinates left to be explored
    while (!queue.isEmpty()) {
      CustomVec2D nextPos = queue.remove();
      // Already processed
      if (processedCoordinates.contains(nextPos)) {
        continue;
      }
      processedCoordinates.add(nextPos);

      Tile t = surface.getTile(nextPos);
      if (!(t instanceof Viewable)) {
        continue;
      }
      Entity entity = getEntity(nextPos);

      if (entity.color == Color.TRANSPARENT && entity.type != EntityType.MONSTER) {
        continue;
      }

      // Get the neighbours
      List<CustomVec2D> neighbours = NavUtils.neighbourCoordinates(nextPos, Level.SIZE, true);
      if (t instanceof Door) {
        // Does not have a lit floor tile next to it, so we assume we cannot see it
        if (neighbours.stream()
            .noneMatch(
                pos ->
                    surface.getTile(pos) instanceof Floor
                        && getEntity(pos).color != Color.TRANSPARENT)) {
          continue;
        }
      }

      // Current tile is visible
      ((Viewable) t).setVisible();
      visibleCoordinates.add(nextPos);

      // Only add all neighbours if it is floor
      if (t instanceof Floor) {
        queue.addAll(neighbours);
      }
    }
  }

  private void resetVisibility() {
    // First reset visibility of all tiles to false
    for (Tile[] row : surface.tiles) {
      for (Tile t : row) {
        if (t instanceof Viewable) {
          ((Viewable) t).setVisibility(false);
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
