package agent.selector;

import agent.iv4xr.AgentState;
import agent.navigation.NetHackSurface;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.*;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nl.uu.cs.aplib.utils.Pair;

public class TileSelector extends Selector<Pair<Integer, Tile>> {
  public static final TileSelector wallSelector =
      new TileSelector(
          SelectionType.CLOSEST,
          Wall.class,
          t -> {
            Wall w = (Wall) t.snd;
            return w.timesSearched < 10;
          });

  public static final TileSelector closedDoorSelector =
      new TileSelector(
          SelectionType.CLOSEST,
          Door.class,
          t -> {
            Door d = (Door) t.snd;
            return !d.isOpen;
          });

  public static final TileSelector lockedDoorSelector =
      new TileSelector(
          SelectionType.CLOSEST,
          Door.class,
          t -> {
            Door d = (Door) t.snd;
            return d.isLocked && !d.isOpen;
          });

  public static final TileSelector stairDown =
      new TileSelector(
          SelectionType.FIRST,
          Stair.class,
          t -> {
            return ((Stair) t.snd).getClimbType() == Climbable.ClimbType.Descendable;
          });

  final Class tileClass;

  public TileSelector(
      SelectionType selectionType, Class tileClass, Predicate<Pair<Integer, Tile>> predicate) {
    super(selectionType, predicate);
    this.tileClass = tileClass;
  }

  public Pair<Integer, Tile> apply(AgentState S) {
    List<Pair<Integer, Tile>> tiles = new ArrayList<>();
    NetHackSurface surface = S.area();
    if (tileClass != null) {
      HashSet<IntVec2D> tilesOfType = surface.getCoordinatesOfTileType(tileClass);
      if (tilesOfType == null) {
        return null;
      }
      for (IntVec2D pos : tilesOfType) {
        tiles.add(
            new Pair<Integer, Tile>(NavUtils.levelNr(S.worldmodel.position), surface.getTile(pos)));
      }
    } else {
      for (Tile[] row : surface.tiles) {
        for (Tile tile : row) {
          if (tile != null) {
            tiles.add(new Pair<>(NavUtils.levelNr(S.worldmodel.position), tile));
          }
        }
      }
    }
    return apply(tiles, S);
  }

  @Override
  public Pair<Integer, Tile> apply(List<Pair<Integer, Tile>> tiles, AgentState S) {
    List<Pair<Integer, Tile>> filteredTiles = filter(tiles);
    return select(filteredTiles, S);
  }

  @Override
  public Pair<Integer, Tile> select(List<Pair<Integer, Tile>> tiles, AgentState S) {
    if (tiles.isEmpty()) {
      return null;
    }

    if (selectionType == SelectionType.FIRST || selectionType == SelectionType.LAST) {
      return super.select(tiles, S);
    }

    int n = tiles.size();
    // Goes wrong for multiple levels
    IntVec2D agentPos = NavUtils.loc2(S.worldmodel.position);
    float min = NetHackSurface.heuristic(agentPos, tiles.get(0).snd.pos);
    float max = min;
    int minIndex = 0, maxIndex = 0;
    for (int i = 1; i < n; i++) {
      float dist = NetHackSurface.heuristic(agentPos, tiles.get(i).snd.pos);
      if (dist < min) {
        min = dist;
        minIndex = i;
      } else if (dist > max) {
        max = dist;
        maxIndex = i;
      }
    }

    if (selectionType == SelectionType.CLOSEST) {
      System.out.printf(
          "Closest is at %s with distance %s. Current position is %s%n",
          tiles.get(minIndex), min, S.worldmodel.position);
      return tiles.get(minIndex);
    } else if (selectionType == SelectionType.FARTHEST) {
      return tiles.get(maxIndex);
    } else {
      throw new UnknownError("SelectionType not implemented: " + selectionType);
    }
  }

  private List<Pair<Integer, Tile>> filter(List<Pair<Integer, Tile>> tiles) {
    Stream<Pair<Integer, Tile>> stream;
    if (tileClass != null && predicate != null) {
      stream =
          tiles.stream()
              .filter(t -> Objects.equals(tileClass, t.snd.getClass()) && predicate.test(t));
    } else if (tileClass != null) {
      stream = tiles.stream().filter(t -> Objects.equals(tileClass, t.snd.getClass()));
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
