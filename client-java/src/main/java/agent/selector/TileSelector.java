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
  public static final TileSelector adjacentWallSelector =
      new TileSelector(
          SelectionType.CLOSEST,
          Wall.class,
          t -> {
            Wall w = (Wall) t.snd;
            return w.timesSearched < 10;
          },
          true);

  public static final TileSelector adjacentClosedDoorSelector =
      new TileSelector(
          SelectionType.CLOSEST,
          Door.class,
          t -> {
            Door d = (Door) t.snd;
            return !d.isOpen;
          },
          true);

  public static final TileSelector closedDoorSelector =
      new TileSelector(
          SelectionType.CLOSEST,
          Door.class,
          t -> {
            Door d = (Door) t.snd;
            return !d.isOpen;
          },
          false);

  public static final TileSelector lockedDoorSelector =
      new TileSelector(
          SelectionType.CLOSEST,
          Door.class,
          t -> {
            Door d = (Door) t.snd;
            return d.isLocked && !d.isOpen;
          },
          false);

  public static final TileSelector stairDown =
      new TileSelector(
          SelectionType.FIRST,
          Stair.class,
          t -> {
            return ((Stair) t.snd).getClimbType() == Climbable.ClimbType.Descendable;
          },
          false);

  final Class tileClass;

  public TileSelector(
      SelectionType selectionType,
      Class tileClass,
      Predicate<Pair<Integer, Tile>> predicate,
      boolean adjacent) {
    super(selectionType, predicate, adjacent);
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
    if (adjacent) {
      filteredTiles = NavUtils.adjacentPositions(filteredTiles, S);
    }
    return select(filteredTiles, S);
  }

  @Override
  public Pair<Integer, Tile> select(List<Pair<Integer, Tile>> tiles, AgentState S) {
    if (tiles.isEmpty()) {
      return null;
    }

    // The selection type does not matter if there is no choice
    int n = tiles.size();
    if (n == 1 || selectionType == SelectionType.FIRST) {
      return tiles.get(0);
    }

    if (selectionType == SelectionType.LAST) {
      return tiles.get(n - 1);
    }

    if (selectionType == SelectionType.CLOSEST) {
      return selectClosest(tiles, S);
    }

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

    if (selectionType == SelectionType.FARTHEST) {
      return tiles.get(maxIndex);
    } else {
      throw new UnknownError("SelectionType not implemented: " + selectionType);
    }
  }

  public Pair<Integer, Tile> selectClosest(List<Pair<Integer, Tile>> tiles, AgentState S) {
    int n = tiles.size();
    NetHackSurface surface = S.area();
    Tile agentTile = NavUtils.toTile(S.worldmodel.position);
    List<Tile> shortestPath = null;
    Tile closestTile = null;

    for (Pair<Integer, Tile> tile : tiles) {
      assert tile.fst.equals(NavUtils.levelNr(S.worldmodel.position))
          : "The level must be the same for closest/furthest navigation";

      // Cannot be shorter since distance is at least equal
      if (shortestPath != null
          && surface.manhattanDistance(agentTile, tile.snd) >= shortestPath.size()) {
        continue;
      }

      List<Tile> path = surface.findPath(agentTile, tile.snd);
      if (path == null) {
        continue;
      }
      if (shortestPath == null || path.size() < shortestPath.size()) {
        shortestPath = path;
        if (path.isEmpty()) {
          closestTile = agentTile;
        } else {
          closestTile = path.get(path.size() - 1);
        }
      }
    }

    if (shortestPath == null) {
      return null;
    } else if (shortestPath.isEmpty()) {
      return NavUtils.loc3(S.worldmodel.position);
    }
    return new Pair<>(NavUtils.levelNr(S.worldmodel.position), closestTile);
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
      return tiles;
    }
    return stream.collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return String.format(
        "TileSelector: %s %s (hasPredicate=%b)", selectionType, tileClass, predicate != null);
  }
}
