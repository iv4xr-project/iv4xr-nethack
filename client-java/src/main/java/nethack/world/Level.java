package nethack.world;

import agent.navigation.hpastar.Size;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Tile;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import nethack.enums.EntityClass;
import nethack.object.Entity;
import nethack.object.Monster;
import nethack.object.Symbol;
import nethack.world.tiles.*;
import util.ColoredStringBuilder;
import util.CustomVec2D;
import util.Loggers;

public class Level implements Serializable {
  public static final Size SIZE = new Size(79, 21);
  public final Symbol[][] map = new Symbol[SIZE.height][SIZE.width];
  public List<Monster> monsters = new ArrayList<>();
  public List<Entity> entities = new ArrayList<>();
  public final Map<EntityClass, HashSet<CustomVec2D>> entityTypesMap = new HashMap<>();

  public final List<Tile> changedTiles = new ArrayList<>();
  public final List<CustomVec2D> changedMapCoordinates = new ArrayList<>();
  public final List<Entity> changedEntities = new ArrayList<>();

  public Surface surface = new Surface();

  public Level(Symbol[][] symbols, Tile[][] tiles, List<Monster> monsters, List<Entity> entities) {
    updateLevel(symbols, tiles, monsters, entities);
  }

  public void updateLevel(
      Symbol[][] newSymbols, Tile[][] newTiles, List<Monster> monsters, List<Entity> entities) {
    this.monsters = monsters;
    this.entities = entities;
    updateSymbols(newSymbols);
    updateTiles(newTiles);
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

  private void updateSymbols(Symbol[][] symbols) {
    changedMapCoordinates.clear();

    // If it is a subsequent observation, only give coordinates of fields that changed
    for (int x = 0; x < SIZE.width; x++) {
      for (int y = 0; y < SIZE.height; y++) {
        CustomVec2D pos = new CustomVec2D(x, y);
        Symbol prevSymbol = getSymbol(pos);
        Symbol newSymbol = symbols[y][x];
        if (Objects.equals(prevSymbol, newSymbol)) {
          continue;
        }

        setSymbol(pos, newSymbol);
        changedMapCoordinates.add(pos);
      }
    }

    for (int x = 0; x < SIZE.width; x++) {
      for (int y = 0; y < SIZE.height; y++) {
        CustomVec2D pos = new CustomVec2D(x, y);
      }
    }

    Loggers.NetHackLogger.debug("%d entity(s) changes in the level", changedMapCoordinates.size());
  }

  private void updateTiles(Tile[][] tiles) {
    changedTiles.clear();

    Set<CustomVec2D> bouldersPositions =
        entities.stream()
            .filter(entity -> entity.entityInfo.name.equalsIgnoreCase("boulder"))
            .map(entity -> entity.pos)
            .collect(Collectors.toSet());
    // If it is a subsequent observation, only give coordinates of fields that changed
    for (int x = 0; x < SIZE.width; x++) {
      for (int y = 0; y < SIZE.height; y++) {
        CustomVec2D pos = new CustomVec2D(x, y);
        boolean hasBoulder = bouldersPositions.contains(pos);
        Tile tile = surface.getTile(pos);
        if (hasBoulder) {
          if (!(tile instanceof Boulder)) {
            Boulder boulder = new Boulder(tiles[y][x].loc);
            changedTiles.add(boulder);
            boulder.setVisibility(((Viewable) tiles[y][x]).getVisibility());
          }
          if (tile != null) {
            ((Viewable) tile).setVisibility(((Viewable) tiles[y][x]).getVisibility());
          }
        } else if (!Objects.equals(tile, tiles[y][x])) {
          changedTiles.add(tiles[y][x]);
        }
      }
    }

    surface.updateTiles(changedTiles);
    Loggers.NetHackLogger.debug("%d tile(s) changes in the level", changedMapCoordinates.size());
  }

  private void markShops() {
    List<CustomVec2D> shopkeepers =
        monsters.stream()
            .filter(monster -> monster.monsterInfo.name.equals("shopkeeper"))
            .map(monster -> monster.pos)
            .toList();
    if (shopkeepers.isEmpty()) {
      return;
    }

    // No shopkeepers
    Set<CustomVec2D> processedCoordinates = new HashSet<>();
    Queue<CustomVec2D> queue = new LinkedList<>(shopkeepers);
    while (!queue.isEmpty()) {
      CustomVec2D nextPos = queue.remove();
      // Already processed
      if (processedCoordinates.contains(nextPos)) {
        continue;
      }
      processedCoordinates.add(nextPos);

      Tile t = surface.getTile(nextPos);
      if (!(t instanceof Floor f)) {
        continue;
      }
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

  public Symbol getSymbol(CustomVec2D p) {
    return getSymbol(p.x, p.y);
  }

  public Symbol getSymbol(int x, int y) {
    return map[y][x];
  }

  public void setSymbol(CustomVec2D p, Symbol symbol) {
    map[p.y][p.x] = symbol;
  }

  @Override
  public String toString() {
    ColoredStringBuilder csb = new ColoredStringBuilder();
    for (int y = 0; y < SIZE.height; y++) {
      for (int x = 0; x < SIZE.width; x++) {
        Symbol symbol = getSymbol(x, y);
        if (symbol == null) {
          csb.append(' ');
        } else {
          csb.setColor(symbol.color);
          csb.append(symbol.symbol);
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
