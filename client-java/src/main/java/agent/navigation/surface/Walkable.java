package agent.navigation.surface;

import agent.navigation.hpastar.smoother.Direction;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface Walkable {
  public default boolean isWalkable() {
    return getWalkableType() != null;
  }

  public WalkableType getWalkableType();

  public default Set<Direction> movementDirections() {
    WalkableType walkableType = getWalkableType();
    assert walkableType != WalkableType.Custom : "No default implementation for custom";

    if (walkableType == WalkableType.Straight) {
      return new HashSet<>(
          Arrays.asList(Direction.North, Direction.South, Direction.West, Direction.East));
    } else {
      return new HashSet<>(
          Arrays.asList(
              Direction.North,
              Direction.South,
              Direction.East,
              Direction.West,
              Direction.NorthWest,
              Direction.NorthEast,
              Direction.SouthWest,
              Direction.SouthEast));
    }
  }

  public enum WalkableType {
    Straight,
    Diagonal,
    Custom,
  }
}
