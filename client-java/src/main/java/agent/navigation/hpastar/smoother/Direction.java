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
    switch (direction) {
      case North:
        return new Command(CommandEnum.DIRECTION_N);
      case South:
        return new Command(CommandEnum.DIRECTION_S);
      case West:
        return new Command(CommandEnum.DIRECTION_W);
      case East:
        return new Command(CommandEnum.DIRECTION_E);
      case NorthEast:
        return new Command(CommandEnum.DIRECTION_NE);
      case NorthWest:
        return new Command(CommandEnum.DIRECTION_NW);
      case SouthEast:
        return new Command(CommandEnum.DIRECTION_SE);
      case SouthWest:
        return new Command(CommandEnum.DIRECTION_SW);
      default:
        throw new IllegalArgumentException("Direction not known");
    }
  }

  public Direction oppositeDirection() {
    switch (this) {
      case North:
        return South;
      case South:
        return North;
      case East:
        return West;
      case West:
        return East;
      case NorthWest:
        return SouthEast;
      case NorthEast:
        return SouthWest;
      case SouthEast:
        return NorthWest;
      case SouthWest:
        return NorthEast;
    }

    throw new IllegalArgumentException("Direction opposite unknown");
  }
}
