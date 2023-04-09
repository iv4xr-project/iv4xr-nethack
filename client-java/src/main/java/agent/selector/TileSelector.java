package agent.selector;

import agent.iv4xr.AgentState;
import agent.navigation.surface.Climbable;
import agent.navigation.surface.Tile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nethack.world.Surface;
import nethack.world.tiles.Door;
import nethack.world.tiles.Stair;
import util.CustomVec2D;
import util.CustomVec3D;

public class TileSelector extends Selector<Tile> {
  // Was adjacent
  //  public static final TileSelector adjacentWallSelector =
  //      new TileSelector().ofClass(Wall.class).predicate((tile, S) -> ((Wall)tile).timesSearched <
  // 10);
  public static final TileSelector adjacentClosedDoorSelector =
      new TileSelector()
          .selectionType(SelectionType.STRAIGHT_ADJACENT)
          .ofClass(Door.class)
          .predicate(
              (tile, S) -> {
                Door d = (Door) tile;
                return (d.closed || d.locked) && !d.isShopDoor;
              });
  public static final TileSelector stairDown =
      new TileSelector()
          .selectionType(SelectionType.FIRST)
          .ofClass(Stair.class)
          .predicate((tile, S) -> ((Stair) tile).getClimbType() == Climbable.ClimbType.Down);

  Class tileClass;

  public TileSelector() {}

  public TileSelector ofClass(Class tileClass) {
    this.tileClass = tileClass;
    return this;
  }

  public TileSelector selectionType(SelectionType selectionType) {
    super.selectionType(selectionType);
    return this;
  }

  public TileSelector sameLvl(boolean onlySameLevel) {
    super.sameLvl(onlySameLevel);
    return this;
  }

  public TileSelector predicate(BiPredicate<Tile, AgentState> predicate) {
    super.predicate(predicate);
    return this;
  }

  public TileSelector globalPredicate(Predicate<AgentState> predicate) {
    super.globalPredicate(predicate);
    return this;
  }

  public Tile apply(AgentState S) {
    List<Tile> coordinates = new ArrayList<>();
    Surface surface = S.area();
    assert tileClass != null : "Tile must be of a certain class";
    HashSet<CustomVec2D> tilesOfType = surface.getCoordinatesOfTileType(tileClass);
    if (tilesOfType == null) {
      return null;
    }
    for (CustomVec2D pos : tilesOfType) {
      coordinates.add(surface.getTile(pos));
    }

    return apply(coordinates, S);
  }

  @Override
  public Tile apply(List<Tile> coordinates, AgentState S) {
    List<Tile> filteredTiles = filter(coordinates, S);
    //    if (adjacent) {
    //      List<CustomVec3D> tileLocations =
    //          filteredTiles.stream().map(tile -> tile.loc).collect(Collectors.toList());
    //      tileLocations = NavUtils.adjacentPositions(tileLocations, S);
    //      filteredTiles =
    //          tileLocations.stream()
    //              .map(loc -> S.hierarchicalNav().getTile(loc))
    //              .collect(Collectors.toList());
    //    }
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

  public List<Tile> filter(List<Tile> tiles, AgentState S) {
    if (globalPredicate != null && !globalPredicate.test(S)) {
      return new ArrayList<>();
    }

    Stream<Tile> stream = tiles.stream();
    if (tileClass != null) {
      stream = tiles.stream().filter(tile -> Objects.equals(tileClass, tile.getClass()));
    }
    if (predicate != null) {
      stream = tiles.stream().filter(tile -> predicate.test(tile, S));
    }
    return stream.collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return String.format(
        "TileSelector: %s %s (hasPredicate=%b)", selectionType, tileClass, predicate != null);
  }
}
