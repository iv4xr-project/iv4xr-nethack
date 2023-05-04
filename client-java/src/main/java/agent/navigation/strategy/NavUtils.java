package agent.navigation.strategy;

import agent.iv4xr.AgentState;
import agent.navigation.GridSurface;
import agent.navigation.HierarchicalNavigation;
import agent.navigation.hpastar.Size;
import agent.navigation.hpastar.search.Path;
import agent.navigation.hpastar.smoother.Direction;
import agent.navigation.surface.Climbable;
import agent.navigation.surface.Tile;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import nethack.NetHack;
import nethack.enums.CommandEnum;
import nethack.object.Command;
import nethack.world.tiles.Stair;
import util.CustomVec2D;
import util.CustomVec3D;

public class NavUtils {
  /**
   * Distance in terms of path-length from the agent that owns S to the entity e. It uses
   * adjustedFindPath to calculate the path.
   */
  public static Path<CustomVec3D> addLevelNr(Path<CustomVec2D> path, int lvl) {
    if (path == null) {
      return null;
    }
    List<CustomVec3D> nodes =
        path.nodes.stream().map(pos -> new CustomVec3D(lvl, pos)).collect(Collectors.toList());
    return new Path<>(nodes, path.cost);
  }

  public static List<CustomVec3D> addLevelNr(List<CustomVec2D> coords, int lvl) {
    if (coords == null) {
      return null;
    }
    return coords.stream().map(pos -> new CustomVec3D(lvl, pos)).collect(Collectors.toList());
  }

  /**
   * Calculate a path from (x0,y0) in maze-0 to (x1,y1) in maze-1. The method will pretend that the
   * source (x0,y0) and destination (x1,y1) are non-blocking (even if they are, e.g. if one of them
   * is an occupied tile).
   */
  public static Path<CustomVec3D> adjustedFindPath(
      AgentState state, CustomVec3D oldLocation, CustomVec3D newLocation) {
    HierarchicalNavigation nav = state.hierarchicalNav();
    return nav.findPath(oldLocation, newLocation);
  }

  public static CustomVec2D posInDirection(CustomVec2D pos, Direction direction) {
    return switch (direction) {
      case North -> new CustomVec2D(pos.x, pos.y - 1);
      case South -> new CustomVec2D(pos.x, pos.y + 1);
      case East -> new CustomVec2D(pos.x + 1, pos.y);
      case West -> new CustomVec2D(pos.x - 1, pos.y);
      case NorthEast -> new CustomVec2D(pos.x + 1, pos.y - 1);
      case NorthWest -> new CustomVec2D(pos.x - 1, pos.y - 1);
      case SouthEast -> new CustomVec2D(pos.x + 1, pos.y + 1);
      case SouthWest -> new CustomVec2D(pos.x - 1, pos.y + 1);
    };
  }

  public static List<CustomVec3D> adjacentPositions(List<CustomVec3D> locations, AgentState S) {
    CustomVec3D agentLoc = S.loc();
    assert locations.stream().allMatch(loc -> loc.lvl == agentLoc.lvl)
        : "All must be on same level for now";
    GridSurface surface = S.area();
    Set<CustomVec2D> processedPositions =
        locations.stream().map(entry -> entry.pos).collect(Collectors.toSet());
    Set<CustomVec2D> adjacentPositions = new HashSet<>();

    for (CustomVec2D pos : new ArrayList<>(processedPositions)) {
      List<CustomVec2D> neighbours =
          NavUtils.neighbourCoordinates(pos, surface.hierarchicalMap.size, true);
      for (CustomVec2D neighbour : neighbours) {
        if (processedPositions.contains(neighbour)) {
          continue;
        }

        processedPositions.add(neighbour);
        if (surface.concreteMap.passability.cannotEnter(neighbour)) {
          continue;
        }

        adjacentPositions.add(neighbour);
      }
    }

    return adjacentPositions.stream()
        .map(pos -> new CustomVec3D(agentLoc.lvl, pos))
        .collect(Collectors.toList());
  }

  public static NetHack.StepType moveTo(AgentState S, CustomVec3D targetTile) {
    Command command;
    if (S.loc().lvl == targetTile.lvl) {
      command = Direction.getCommand(toDirection(S, targetTile));
    } else {
      Tile tile = S.hierarchicalNav().getTile(S.loc());
      assert tile instanceof Stair : "Level is different however no stairs at location";
      Stair stairTile = (Stair) tile;
      if (stairTile.climbType == Climbable.ClimbType.Down) {
        command = new Command(CommandEnum.MISC_DOWN);
      } else {
        command = new Command(CommandEnum.MISC_UP);
      }
    }

    return S.app().step(List.of(command));
  }

  public static Direction toDirection(AgentState state, CustomVec3D target) {
    CustomVec3D agentLoc = state.loc();
    assert agentLoc.lvl == target.lvl : "Direction must be on same level";
    CustomVec2D targetPos = target.pos;
    CustomVec2D agentPos = agentLoc.pos;
    assert CustomVec2D.adjacent(agentPos, targetPos, true);

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

  public static CustomVec3D nextLoc(Path<CustomVec3D> path) {
    if (path == null || path.nextNode() == null) {
      return null;
    }
    return path.nextNode();
  }

  public static boolean withinBounds(CustomVec2D pos, Size size) {
    return pos.x >= 0 && pos.y >= 0 && pos.x < size.width && pos.y < size.height;
  }

  public static List<CustomVec2D> neighbourCoordinates(
      CustomVec2D pos, Size size, boolean allowDiagonal) {
    List<CustomVec2D> neighbourCoords = new ArrayList<>(2);
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
      neighbourCoords.add(new CustomVec2D(pos.x, below));
      neighbourCoords.add(new CustomVec2D(pos.x, above));
      neighbourCoords.add(new CustomVec2D(left, pos.y));
      neighbourCoords.add(new CustomVec2D(right, pos.y));

      if (allowDiagonal) {
        neighbourCoords.add(new CustomVec2D(right, above));
        neighbourCoords.add(new CustomVec2D(left, above));
        neighbourCoords.add(new CustomVec2D(right, below));
        neighbourCoords.add(new CustomVec2D(left, below));
      }

      return neighbourCoords;
    }

    if (leftInsideMap) {
      neighbourCoords.add(new CustomVec2D(left, pos.y));
      if (allowDiagonal) {
        if (belowInsideMap) {
          neighbourCoords.add(new CustomVec2D(left, below));
        }
        if (aboveInsideMap) {
          neighbourCoords.add(new CustomVec2D(left, above));
        }
      }
    }
    if (aboveInsideMap) {
      neighbourCoords.add(new CustomVec2D(pos.x, above));
    }
    if (belowInsideMap) {
      neighbourCoords.add(new CustomVec2D(pos.x, below));
    }
    if (rightInsideMap) {
      neighbourCoords.add(new CustomVec2D(right, pos.y));
      if (allowDiagonal) {
        if (belowInsideMap) {
          neighbourCoords.add(new CustomVec2D(right, below));
        }
        if (aboveInsideMap) {
          neighbourCoords.add(new CustomVec2D(right, above));
        }
      }
    }

    return neighbourCoords;
  }

  /** check if the location of the entity e is reachable from the agent current position. */
  public static boolean isReachable(AgentState S, WorldEntity e) {
    return adjustedFindPath(S, S.loc(), new CustomVec3D(e.position)) != null;
  }
}
