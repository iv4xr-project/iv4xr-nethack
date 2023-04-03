package nethack.world;

import agent.navigation.hpastar.Size;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Tile;
import java.util.*;
import nethack.enums.Color;
import nethack.enums.EntityType;
import nethack.object.Entity;
import nethack.world.tiles.*;
import util.ColoredStringBuilder;
import util.CustomVec2D;
import util.Loggers;

public class Level {
  public static final Size SIZE = new Size(79, 21);
  public final Entity[][] map = new Entity[SIZE.height][SIZE.width];
  public final Map<EntityType, HashSet<CustomVec2D>> entityTypesMap = new HashMap<>();

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
    markShops();
  }

  public void markShopDoors(CustomVec2D playerPos) {
    Iterable<CustomVec2D> neighbours = surface.neighbourCoordinates(playerPos, true);
    for (CustomVec2D neighbour : neighbours) {
      Tile tile = surface.getTile(neighbour);
      if (tile instanceof Door) {
        ((Door) tile).isShopDoor = true;
      }
    }
  }

  private void updateEntities(Entity[][] entities) {
    changedEntities.clear();

    // If it is a subsequent observation, only give coordinates of fields that changed
    for (int x = 0; x < SIZE.width; x++) {
      for (int y = 0; y < SIZE.height; y++) {
        CustomVec2D pos = new CustomVec2D(x, y);
        Entity prevEntity = getEntity(pos);
        Entity newEntity = entities[y][x];
        if (Objects.equals(prevEntity, newEntity)) {
          continue;
        }

        // Remove old entity
        if (prevEntity != null) {
          assert entityTypesMap.containsKey(prevEntity.type);
          entityTypesMap.get(prevEntity.type).remove(pos);
        }

        setEntity(pos, newEntity);
        changedEntities.add(pos);
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
        Entity entity = getEntity(pos);
        if (entity != null && entity.type == EntityType.BOULDER) {
          if (!(surface.getTile(pos) instanceof Boulder)) {
            changedTiles.add(new Boulder(tiles[y][x].loc));
          }
        } else if (!Objects.equals(surface.getTile(pos), tiles[y][x])) {
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
        if (e == null) {
          continue;
        }

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
      setTileVisible(neighbourTile);
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
      if (entity == null) {
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
                        && getEntity(pos) != null
                        && getEntity(pos).color != Color.TRANSPARENT)) {
          continue;
        }
      }

      // Unlit floor
      if (entity.color == Color.TRANSPARENT && entity.type == EntityType.FLOOR) {
        continue;
      }

      // Current tile is visible
      setTileVisible(t);

      // Only add all neighbours if it is floor
      if (t instanceof Floor) {
        queue.addAll(neighbours);
      }
    }
  }

  private void markShops() {
    if (!entityTypesMap.containsKey(EntityType.SHOPKEEPER)) {
      return;
    }

    // No shopkeepers
    Set<CustomVec2D> processedCoordinates = new HashSet<>();
    Queue<CustomVec2D> queue = new LinkedList<>(entityTypesMap.get(EntityType.SHOPKEEPER));
    while (!queue.isEmpty()) {
      CustomVec2D nextPos = queue.remove();
      // Already processed
      if (processedCoordinates.contains(nextPos)) {
        continue;
      }
      processedCoordinates.add(nextPos);

      Tile t = surface.getTile(nextPos);
      if (!(t instanceof Floor)) {
        continue;
      }
      Floor f = (Floor) t;
      // Tile was already marked as shop
      if (f.isShop()) {
        continue;
      }

      f.isShop = true;

      // Get the neighbours
      List<CustomVec2D> neighbours = NavUtils.neighbourCoordinates(nextPos, Level.SIZE, true);
      queue.addAll(neighbours);
    }
  }

  private void setTileVisible(Tile tile) {
    ((Viewable) tile).setVisible();
    surface.markAsSeen(tile.pos);
    visibleCoordinates.add(tile.pos);
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
    map[p.y][p.x] = entity;

    if (entity == null) {
      return;
    }

    if (!entityTypesMap.containsKey(entity.type)) {
      entityTypesMap.put(entity.type, new HashSet<>());
    }

    entityTypesMap.get(entity.type).add(p);
  }

  @Override
  public String toString() {
    //    ColoredStringBuilder csb = new ColoredStringBuilder();
    //    String[] game = gameState.toString().split(System.lineSeparator());
    //    String[] navigation = area().toString().split(System.lineSeparator());
    //
    //    String tripleFormatString =
    //            String.format(
    //                    "%%-%ds %%-%ds %%-%ds%n", Level.SIZE.width, Level.SIZE.width,
    // Level.SIZE.width);
    //    String doubleFormatString =
    //            String.format("%%-%ds %%-%ds%n", 2 * Level.SIZE.width + 1, Level.SIZE.width);
    //    int n = Level.SIZE.height;
    //
    //    csb.appendf(doubleFormatString, game[0], hierarchicalMap[0]);
    //
    //    for (int i = 0; i < n; i++) {
    //      csb.appendf(tripleFormatString, game[i + 1], navigation[i], hierarchicalMap[i + 1]);
    //    }
    //
    //    csb.appendf(doubleFormatString, game[n + 1], hierarchicalMap[n + 1]);
    //    csb.appendf(
    //            String.format("%%-%ds %%-%ds%n", Level.SIZE.width * 2 + 40, Level.SIZE.width),
    //            game[n + 2],
    //            hierarchicalMap[n + 2]);
    //    csb.appendf(tripleFormatString, "", "", hierarchicalMap[n + 3]);
    //    System.out.print(csb);

    //    return surface.toString();

    ColoredStringBuilder csb = new ColoredStringBuilder();
    for (int y = 0; y < SIZE.height; y++) {
      for (int x = 0; x < SIZE.width; x++) {
        Entity entity = getEntity(x, y);
        if (entity == null) {
          csb.append(' ');
        } else {
          csb.setColor(entity.color);
          csb.append(entity.symbol);
        }
      }
      csb.resetColor();
      if (y != map.length - 1) {
        csb.newLine();
      }
    }
    return csb.toString();
  }
}
