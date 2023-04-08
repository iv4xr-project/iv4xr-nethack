package agent.selector;

import agent.iv4xr.AgentState;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nethack.world.Surface;
import nethack.world.tiles.Door;
import nethack.world.tiles.Stair;
import nethack.world.tiles.Wall;
import util.CustomVec2D;
import util.CustomVec3D;

public class TileSelector extends Selector<Tile> {
  public static final TileSelector adjacentWallSelector =
      new TileSelector(
          SelectionType.SHORTEST,
          Wall.class,
          t -> {
            Wall w = (Wall) t;
            return w.timesSearched < 10;
          },
          true);

  public static final TileSelector adjacentClosedDoorSelector =
      new TileSelector(
          SelectionType.STRAIGHT_ADJACENT,
          Door.class,
          t -> {
            Door d = (Door) t;
            return (d.closed || d.locked) && !d.isShopDoor;
          },
          false);

  public static final TileSelector stairDown =
      new TileSelector(
          SelectionType.FIRST,
          Stair.class,
          t -> {
            return ((Stair) t).getClimbType() == Climbable.ClimbType.Down;
          },
          false);

  final Class tileClass;

  public TileSelector(
      SelectionType selectionType, Class tileClass, Predicate<Tile> predicate, boolean adjacent) {
    super(selectionType, predicate, adjacent);
    this.tileClass = tileClass;
  }

  public Tile apply(AgentState S) {
    List<Tile> coordinates = new ArrayList<>();
    int lvl = S.loc().lvl;
    Surface surface = S.area();
    if (tileClass != null) {
      HashSet<CustomVec2D> tilesOfType = surface.getCoordinatesOfTileType(tileClass);
      if (tilesOfType == null) {
        return null;
      }
      for (CustomVec2D pos : tilesOfType) {
        coordinates.add(surface.getTile(pos));
      }
    } else {
      for (Tile[] row : surface.tiles) {
        for (Tile tile : row) {
          if (tile != null) {
            coordinates.add(surface.getTile(tile.pos));
          }
        }
      }
    }
    return apply(coordinates, S);
  }

  @Override
  public Tile apply(List<Tile> coordinates, AgentState S) {
    List<Tile> filteredTiles = filter(coordinates);
    if (adjacent) {
      List<CustomVec3D> tileLocations =
          filteredTiles.stream().map(tile -> tile.loc).collect(Collectors.toList());
      tileLocations = NavUtils.adjacentPositions(tileLocations, S);
      filteredTiles =
          tileLocations.stream()
              .map(loc -> S.hierarchicalNav().getTile(loc))
              .collect(Collectors.toList());
    }
    return select(filteredTiles, S);
  }

  @Override
  public Tile select(List<Tile> tiles, AgentState S) {
    List<CustomVec3D> coordinates =
        tiles.stream().map(tile -> tile.loc).collect(Collectors.toList());
    Integer index = selectIndex(coordinates, S);
    if (index == null) {
      return null;
    }
    return tiles.get(index);
  }

  public List<Tile> filter(List<Tile> tiles) {
    if (tileClass == null && predicate == null) {
      return tiles;
    }

    Stream<Tile> stream;
    if (tileClass != null && predicate != null) {
      stream =
          tiles.stream()
              .filter(tile -> Objects.equals(tileClass, tile.getClass()) && predicate.test(tile));
    } else if (tileClass != null) {
      stream = tiles.stream().filter(tile -> Objects.equals(tileClass, tile.getClass()));
    } else {
      stream = tiles.stream().filter(tile -> predicate.test(tile));
    }
    return stream.collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return String.format(
        "TileSelector: %s %s (hasPredicate=%b)", selectionType, tileClass, predicate != null);
  }
}
