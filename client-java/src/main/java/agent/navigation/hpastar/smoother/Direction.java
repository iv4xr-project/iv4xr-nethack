//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:35
//

package agent.navigation.hpastar.smoother;

import nethack.enums.CommandEnum;
import nethack.object.Command;

public enum Direction {
  North,
  East,
  South,
  West,
  NorthEast,
  SouthEast,
  SouthWest,
  NorthWest;

  public static Command getCommand(Direction direction) {
    return switch (direction) {
      case North -> new Command(CommandEnum.DIRECTION_N);
      case South -> new Command(CommandEnum.DIRECTION_S);
      case West -> new Command(CommandEnum.DIRECTION_W);
      case East -> new Command(CommandEnum.DIRECTION_E);
      case NorthEast -> new Command(CommandEnum.DIRECTION_NE);
      case NorthWest -> new Command(CommandEnum.DIRECTION_NW);
      case SouthEast -> new Command(CommandEnum.DIRECTION_SE);
      case SouthWest -> new Command(CommandEnum.DIRECTION_SW);
      default -> throw new IllegalArgumentException("Direction not known");
    };
  }

  public Direction oppositeDirection() {
    return switch (this) {
      case North -> South;
      case South -> North;
      case East -> West;
      case West -> East;
      case NorthWest -> SouthEast;
      case NorthEast -> SouthWest;
      case SouthEast -> NorthWest;
      case SouthWest -> NorthEast;
    };
  }
}
