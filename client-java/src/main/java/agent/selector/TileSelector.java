package agent.selector;

import agent.iv4xr.AgentState;
import agent.navigation.surface.Climbable;
import agent.navigation.surface.Tile;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nethack.world.Surface;
import nethack.world.tiles.*;
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
                return !d.isWalkable() && !d.isShopDoor;
              });
  public static final TileSelector stairDown =
      new TileSelector()
          .selectionType(SelectionType.SHORTEST)
          .ofClass(Stair.class)
          .predicate((tile, S) -> ((Stair) tile).getClimbType() == Climbable.ClimbType.Down);

  public static final TileSelector water =
      new TileSelector()
          .selectionType(SelectionType.SHORTEST)
          .ofClass(Set.of(Pool.class, Water.class, Moat.class));

  Set<Class> tileClasses;

  public TileSelector() {}

  public TileSelector ofClass(Class tileClass) {
    this.tileClasses = new HashSet<>();
    tileClasses.add(tileClass);
    return this;
  }

  public TileSelector ofClass(Set<Class> tileClasses) {
    this.tileClasses = tileClasses;
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
    assert tileClasses != null : "Tile must be of a certain class";
    HashSet<CustomVec2D> tilesOfType = new HashSet<>();
    for (Class tileClass : tileClasses) {
      Set<CustomVec2D> coords = surface.getCoordinatesOfTileType(tileClass);
      if (coords != null) {
        tilesOfType.addAll(coords);
      }
    }
    if (tilesOfType.isEmpty()) {
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
    if (tileClasses != null) {
      stream = tiles.stream().filter(tile -> tileClasses.contains(tile.getClass()));
    }
    if (predicate != null) {
      stream = tiles.stream().filter(tile -> predicate.test(tile, S));
    }
    return stream.collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return String.format(
        "TileSelector: %s %s (hasPredicate=%b)", selectionType, tileClasses, predicate != null);
  }
}
