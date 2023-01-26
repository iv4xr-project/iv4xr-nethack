package agent.selector;

import agent.iv4xr.AgentState;
import agent.navigation.NavUtils;
import agent.navigation.NetHackSurface;
import agent.navigation.surface.Door;
import agent.navigation.surface.Stair;
import agent.navigation.surface.Tile;
import agent.navigation.surface.Wall;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TileSelector extends Selector<Tile> {
  public static final TileSelector wallSelector =
      new TileSelector(
          SelectionType.CLOSEST,
          Wall.class,
          t -> {
            Wall w = (Wall) t;
            return w.timesSearched < 10;
          });

  public static final TileSelector closedDoorSelector =
      new TileSelector(
          SelectionType.CLOSEST,
          Door.class,
          t -> {
            Door d = (Door) t;
            return !d.isOpen;
          });

  public static final TileSelector lockedDoorSelector =
      new TileSelector(
          SelectionType.CLOSEST,
          Door.class,
          t -> {
            Door d = (Door) t;
            return d.isLocked && !d.isOpen;
          });

  public static final TileSelector stairDown =
      new TileSelector(
          SelectionType.FIRST,
          Stair.class,
          t -> {
            return !((Stair) t).goesUp;
          });

  Class tileClass;

  public TileSelector(SelectionType selectionType, Class tileClass, Predicate<Tile> predicate) {
    super(selectionType, predicate);
    this.tileClass = tileClass;
  }

  public Tile apply(AgentState S) {
    List<Tile> tiles = new ArrayList<>();
    NetHackSurface surface = S.area();
    if (tileClass != null) {
      HashSet<IntVec2D> tilesOfType = surface.getCoordinatesOfTileType(tileClass);
      if (tilesOfType == null) {
        return null;
      }
      for (IntVec2D pos : tilesOfType) {
        tiles.add(surface.getTile(pos));
      }
    } else {
      for (Tile[] row : S.area().tiles) {
        for (Tile tile : row) {
          if (tile != null) {
            tiles.add(tile);
          }
        }
      }
    }
    return apply(tiles, S);
  }

  @Override
  public Tile apply(List<Tile> tiles, AgentState S) {
    return select(filter(tiles), S);
  }

  @Override
  public Tile select(List<Tile> tiles, AgentState S) {
    if (tiles.isEmpty()) {
      return null;
    }

    if (selectionType == SelectionType.FIRST || selectionType == SelectionType.LAST) {
      return super.select(tiles, S);
    }

    int n = tiles.size();
    // Goes wrong for multiple levels
    IntVec2D agentPos = NavUtils.loc2(S.worldmodel.position);
    float min = NetHackSurface.distSq(agentPos, tiles.get(0).pos);
    float max = min;
    int minIndex = 0, maxIndex = 0;
    for (int i = 1; i < n; i++) {
      float dist = NetHackSurface.distSq(agentPos, tiles.get(i).pos);
      if (dist < min) {
        min = dist;
        minIndex = i;
      } else if (dist > max) {
        max = dist;
        maxIndex = i;
      }
    }

    if (selectionType == SelectionType.CLOSEST) {
      return tiles.get(minIndex);
    } else if (selectionType == SelectionType.FARTHEST) {
      return tiles.get(maxIndex);
    } else {
      throw new UnknownError("SelectionType not implemented: " + selectionType);
    }
  }

  private List<Tile> filter(List<Tile> tiles) {
    Stream<Tile> stream;
    if (tileClass != null && predicate != null) {
      stream =
          tiles.stream().filter(t -> Objects.equals(tileClass, t.getClass()) && predicate.test(t));
    } else if (tileClass != null) {
      stream = tiles.stream().filter(t -> Objects.equals(tileClass, t.getClass()));
    } else if (predicate != null) {
      stream = tiles.stream().filter(t -> predicate.test(t));
    } else {
      stream = tiles.stream();
    }
    return stream.collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return String.format(
        "TileSelector: %s %s (hasPredicate=%b)", selectionType, tileClass, predicate != null);
  }
}
