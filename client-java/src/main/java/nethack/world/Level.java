package nethack.world;

import agent.navigation.hpastar.Size;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Tile;
import java.util.*;
import java.util.stream.Collectors;
import nethack.enums.Color;
import nethack.enums.EntityClass;
import nethack.enums.SymbolType;
import nethack.object.Entity;
import nethack.object.Monster;
import nethack.object.Symbol;
import nethack.world.tiles.*;
import util.ColoredStringBuilder;
import util.CustomVec2D;
import util.Loggers;

public class Level {
  public static final Size SIZE = new Size(79, 21);
  public final Symbol[][] map = new Symbol[SIZE.height][SIZE.width];
  public List<Monster> monsters = new ArrayList<>();
  public List<Entity> entities = new ArrayList<>();
  public final Map<EntityClass, HashSet<CustomVec2D>> entityTypesMap = new HashMap<>();

  public final List<Tile> changedTiles = new ArrayList<>();
  public final List<CustomVec2D> changedMapCoordinates = new ArrayList<>();
  public final List<Entity> changedEntities = new ArrayList<>();

  public Surface surface = new Surface();
  public Set<CustomVec2D> visibleCoordinates = new HashSet<>();

  public Level(
      CustomVec2D playerPos,
      Symbol[][] symbols,
      Tile[][] tiles,
      List<Monster> monsters,
      List<Entity> entities) {
    updateLevel(playerPos, symbols, tiles, monsters, entities);
  }

  public void updateLevel(
      CustomVec2D playerPos,
      Symbol[][] newSymbols,
      Tile[][] newTiles,
      List<Monster> monsters,
      List<Entity> entities) {
    updateSymbols(newSymbols);
    updateTiles(newTiles);
    this.monsters = monsters;
    this.entities = entities;
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

    Loggers.NetHackLogger.debug("%d entity(s) changes in the level", changedMapCoordinates.size());
  }

  private void updateTiles(Tile[][] tiles) {
    changedTiles.clear();

    // If it is a subsequent observation, only give coordinates of fields that changed
    for (int x = 0; x < SIZE.width; x++) {
      for (int y = 0; y < SIZE.height; y++) {
        CustomVec2D pos = new CustomVec2D(x, y);
        Symbol symbol = getSymbol(pos);
        if (symbol != null && symbol.type == SymbolType.BOULDER) {
          if (!(surface.getTile(pos) instanceof Boulder)) {
            changedTiles.add(new Boulder(tiles[y][x].loc));
          }
        } else if (!Objects.equals(surface.getTile(pos), tiles[y][x])) {
          changedTiles.add(tiles[y][x]);
        }
      }
    }

    surface.updateTiles(changedTiles);

    Loggers.NetHackLogger.debug("%d tile(s) changes in the level", changedMapCoordinates.size());
  }

  private void updateVisibility(CustomVec2D agentPosition) {
    resetVisibility();

    Set<CustomVec2D> visibleFloors = new HashSet<>();
    for (int x = 0; x < SIZE.width; x++) {
      for (int y = 0; y < SIZE.height; y++) {
        Symbol e = getSymbol(x, y);
        if (e == null) {
          continue;
        }

        if (e.type == SymbolType.FLOOR && e.color == Color.GRAY) {
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
      Symbol symbol = getSymbol(nextPos);
      if (symbol == null) {
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
                        && getSymbol(pos) != null
                        && getSymbol(pos).color != Color.TRANSPARENT)) {
          continue;
        }
      }

      // Unlit floor
      if (symbol.color == Color.TRANSPARENT && symbol.type == SymbolType.FLOOR) {
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
    List<CustomVec2D> shopkeepers =
        monsters.stream()
            .filter(monster -> monster.monsterInfo.name.equals("shopkeeper"))
            .map(monster -> monster.pos)
            .collect(Collectors.toList());
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
