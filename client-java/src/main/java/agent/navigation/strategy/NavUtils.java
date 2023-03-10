package agent.navigation.strategy;

import agent.iv4xr.AgentState;
import agent.navigation.GridSurface;
import agent.navigation.HierarchicalNavigation;
import agent.navigation.hpastar.Size;
import agent.navigation.hpastar.smoother.Direction;
import agent.navigation.hpastar.utils.RefSupport;
import agent.navigation.surface.Tile;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import nethack.object.Command;
import nethack.object.Player;
import nl.uu.cs.aplib.utils.Pair;
import util.Loggers;

public class NavUtils {
  /**
   * Distance in terms of path-length from the agent that owns S to the entity e. It uses
   * adjustedFindPath to calculate the path.
   */
  static int distTo(AgentState S, WorldEntity e) {
    List<Pair<Integer, Tile>> path =
        adjustedFindPath(S, NavUtils.loc3(S.worldmodel.position), NavUtils.loc3(e.position));
    if (path == null) {
      return Integer.MAX_VALUE;
    }
    return path.size() - 1;
  }

  /**
   * Calculate a path from (x0,y0) in maze-0 to (x1,y1) in maze-1. The method will pretend that the
   * source (x0,y0) and destination (x1,y1) are non-blocking (even if they are, e.g. if one of them
   * is an occupied tile).
   */
  public static List<Pair<Integer, Tile>> adjustedFindPath(
      AgentState state, int level0, IntVec2D pos0, int level1, IntVec2D pos1) {
    return adjustedFindPath(state, loc3(level0, pos0), loc3(level1, pos1));
  }

  public static List<Pair<Integer, Tile>> adjustedFindPath(
      AgentState state, Pair<Integer, Tile> oldLocation, Pair<Integer, Tile> newLocation) {
    HierarchicalNavigation nav = state.hierarchicalNav;
    //    boolean srcOriginalBlockingState = nav.isBlocking(oldLocation);
    //    boolean destOriginalBlockingState = nav.isBlocking(newLocation);
    //    nav.toggleBlockingOff(oldLocation);
    //    nav.toggleBlockingOff(newLocation);
    List<Pair<Integer, Tile>> path = nav.findPath(oldLocation, newLocation);
    //    nav.setBlockingState(oldLocation, srcOriginalBlockingState);
    //    nav.setBlockingState(newLocation, destOriginalBlockingState);
    return path;
  }

  public static Tile toTile(Vec3 p) {
    return toTile((int) p.x, (int) p.y);
  }

  public static Tile toTile(int x, int y) {
    return new Tile(x, y);
  }

  public static Tile toTile(IntVec2D pos) {
    return new Tile(pos);
  }

  public static IntVec2D posInDirection(IntVec2D pos, Direction direction) {
    switch (direction) {
      case North:
        return new IntVec2D(pos.x, pos.y - 1);
      case South:
        return new IntVec2D(pos.x, pos.y + 1);
      case East:
        return new IntVec2D(pos.x + 1, pos.y);
      case West:
        return new IntVec2D(pos.x - 1, pos.y);
      case NorthEast:
        return new IntVec2D(pos.x + 1, pos.y - 1);
      case NorthWest:
        return new IntVec2D(pos.x - 1, pos.y - 1);
      case SouthEast:
        return new IntVec2D(pos.x + 1, pos.y + 1);
      case SouthWest:
        return new IntVec2D(pos.x - 1, pos.y + 1);
      default:
        throw new IllegalArgumentException("Direction does not exist");
    }
  }

  public static IntVec2D loc2(int x, int y) {
    return new IntVec2D(x, y);
  }

  public static IntVec2D loc2(Vec3 pos) {
    return new IntVec2D((int) pos.x, (int) pos.y);
  }

  static Pair<Integer, Tile> loc3(int levelNr, int x, int y) {
    return loc3(levelNr, loc2(x, y));
  }

  static Pair<Integer, Tile> loc3(int levelNr, IntVec2D pos) {
    return new Pair<>(levelNr, toTile(pos));
  }

  public static Pair<Integer, Tile> loc3(Vec3 pos) {
    return new Pair<>((int) pos.z, toTile(pos));
  }

  public static boolean adjacent(Vec3 vec1, Vec3 vec2, boolean allowDiagonally) {
    return adjacent(toTile(vec1), toTile(vec2), allowDiagonally);
  }

  public static boolean adjacent(IntVec2D pos1, IntVec2D pos2, boolean allowDiagonally) {
    return adjacent(pos1.x, pos1.y, pos2.x, pos2.y, allowDiagonally);
  }

  // Check if two tiles are adjacent.
  public static boolean adjacent(Tile tile1, Tile tile2, boolean allowDiagonally) {
    return adjacent(tile1.pos.x, tile1.pos.y, tile2.pos.x, tile2.pos.y, allowDiagonally);
  }

  public static boolean isDiagonal(IntVec2D pos1, IntVec2D pos2) {
    return pos1.x != pos2.x && pos1.y != pos2.y;
  }

  private static boolean adjacent(int x0, int y0, int x1, int y1, boolean allowDiagonally) {
    int dx = Math.abs(x0 - x1);
    int dy = Math.abs(y0 - y1);

    // Further than 1 away or same tile
    if (dx > 1 || dy > 1 || (dx == 0 && dy == 0)) {
      return false;
    }
    return allowDiagonally || dx == 0 || dy == 0;
  }

  public static List<Pair<Integer, Tile>> adjacentPositions(
      List<Pair<Integer, Tile>> positions, AgentState S) {
    int levelNr = levelNr(S.worldmodel.position);
    assert positions.stream().allMatch(entry -> entry.fst.equals(levelNr))
        : "All must be on same level";
    GridSurface surface = S.area();
    Set<IntVec2D> processedPositions =
        positions.stream().map(entry -> entry.snd.pos).collect(Collectors.toSet());
    Set<IntVec2D> adjacentPositions = new HashSet<>();

    for (IntVec2D pos : new ArrayList<>(processedPositions)) {
      List<IntVec2D> neighbours =
          NavUtils.neighbourCoordinates(pos, surface.hierarchicalMap.size, true);
      for (IntVec2D neighbour : neighbours) {
        if (processedPositions.contains(neighbour)) {
          continue;
        }

        processedPositions.add(neighbour);
        if (surface.concreteMap.passability.cannotEnter(neighbour, new RefSupport<>())) {
          continue;
        }

        adjacentPositions.add(neighbour);
      }
    }

    return adjacentPositions.stream()
        .map(pos -> NavUtils.loc3(levelNr, pos))
        .collect(Collectors.toList());
  }

  public static int levelNr(Vec3 pos) {
    return (int) pos.z;
  }

  /**
   * Give the straight-line distance-square between two entities, if they are in the same maze; else
   * the distance is the difference between mazeIds times some large multiplier (1000000).
   */
  public static float distanceBetweenEntities(WorldEntity e1, WorldEntity e2) {
    int e1_level = levelNr(e1.position);
    int e2_level = levelNr(e2.position);

    if (e1_level == e2_level) {
      Vec3 p1 = e1.position.copy();
      Vec3 p2 = e2.position.copy();
      p1.z = 0;
      p2.z = 0;
      return Vec3.distSq(p1, p2);
    }
    return Math.abs(e1_level - e2_level) * 1000000;
  }

  /**
   * Give the straight-line distance-square between the agent that owns the given state and the
   * given entity e, if they are in the same maze; else the distance is the difference between their
   * mazeIds times some large multiplier (1000000).
   */
  public static float distanceToAgent(AgentState S, WorldEntity e) {
    return distanceBetweenEntities(S.worldmodel.elements.get(Player.ID), e);
  }

  public static WorldModel moveTo(AgentState state, Pair<Integer, Tile> targetTile) {
    Command command = Direction.getCommand(toDirection(state, targetTile));
    return state.env().commands(command);
  }

  public static WorldModel moveTo(AgentState state, Vec3 targetPosition) {
    return moveTo(state, loc3(targetPosition));
  }

  public static Direction toDirection(AgentState state, Pair<Integer, Tile> targetTile) {
    Pair<Integer, Tile> agentTile = NavUtils.loc3(state.worldmodel.position);
    assert agentTile.fst.equals(targetTile.fst) : "Direction must be on same level";
    IntVec2D targetPos = targetTile.snd.pos;
    IntVec2D agentPos = agentTile.snd.pos;
    assert adjacent(agentPos, targetPos, true);

    if (targetPos.y > agentPos.y) {
      if (targetPos.x > agentPos.x) {
        return Direction.SouthEast;
      } else if (targetPos.x < agentPos.x) {
        return Direction.SouthWest;
      } else {
        return Direction.South;
      }
    } else if (targetPos.y < agentPos.y) {
      if (targetPos.x > agentPos.x) {
        return Direction.NorthEast;
      } else if (targetPos.x < agentPos.x) {
        return Direction.NorthWest;
      } else {
        return Direction.North;
      }
    } else if (targetPos.x > agentPos.x) {
      return Direction.East;
    } else {
      return Direction.West;
    }
  }

  public static Pair<Integer, Tile> nextTile(List<Pair<Integer, Tile>> path) {
    if (path == null) {
      Loggers.NavLogger.debug("Path not found");
      return null;
    } else if (path.size() <= 1) {
      Loggers.NavLogger.debug("Already on location, path is length %s", path.size());
      return null;
    } else {
      // The first element is the src itself, so we need to pick the next one:
      return path.get(1);
    }
  }

  public static boolean withinBounds(IntVec2D pos, Size size) {
    return pos.x >= 0 && pos.y >= 0 && pos.x < size.width && pos.y < size.height;
  }

  public static List<IntVec2D> neighbourCoordinates(
      IntVec2D pos, Size size, boolean allowDiagonal) {
    List<IntVec2D> neighbourCoords = new ArrayList<>(2);
    assert withinBounds(pos, size) : "Position outside the boundaries";

    int left = pos.x - 1;
    int right = pos.x + 1;
    int below = pos.y - 1;
    int above = pos.y + 1;

    boolean leftInsideMap = left >= 0;
    boolean rightInsideMap = right < size.width;

    boolean belowInsideMap = below >= 0;
    boolean aboveInsideMap = above < size.height;

    if (leftInsideMap && rightInsideMap && belowInsideMap && aboveInsideMap) {
      neighbourCoords.add(new IntVec2D(pos.x, below));
      neighbourCoords.add(new IntVec2D(pos.x, above));
      neighbourCoords.add(new IntVec2D(left, pos.y));
      neighbourCoords.add(new IntVec2D(right, pos.y));

      if (allowDiagonal) {
        neighbourCoords.add(new IntVec2D(right, above));
        neighbourCoords.add(new IntVec2D(left, above));
        neighbourCoords.add(new IntVec2D(right, below));
        neighbourCoords.add(new IntVec2D(left, below));
      }

      return neighbourCoords;
    }

    if (leftInsideMap) {
      neighbourCoords.add(new IntVec2D(left, pos.y));
      if (allowDiagonal) {
        if (belowInsideMap) {
          neighbourCoords.add(new IntVec2D(left, below));
        }
        if (aboveInsideMap) {
          neighbourCoords.add(new IntVec2D(left, above));
        }
      }
    }
    if (aboveInsideMap) {
      neighbourCoords.add(new IntVec2D(pos.x, above));
    }
    if (belowInsideMap) {
      neighbourCoords.add(new IntVec2D(pos.x, below));
    }
    if (rightInsideMap) {
      neighbourCoords.add(new IntVec2D(right, pos.y));
      if (allowDiagonal) {
        if (belowInsideMap) {
          neighbourCoords.add(new IntVec2D(right, below));
        }
        if (aboveInsideMap) {
          neighbourCoords.add(new IntVec2D(right, above));
        }
      }
    }

    return neighbourCoords;
  }

  /** check if the location of the entity e is reachable from the agent current position. */
  public static boolean isReachable(AgentState S, WorldEntity e) {
    var path = adjustedFindPath(S, loc3(S.worldmodel.position), loc3(e.position));
    return path != null && path.size() > 0;
  }
}
