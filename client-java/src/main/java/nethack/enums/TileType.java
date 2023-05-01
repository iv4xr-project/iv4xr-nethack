package nethack.enums;

public enum TileType {
  STONE(' ', Color.GRAY),
  WALL('W', Color.GRAY),
  TREE('#', Color.GREEN_BRIGHT),
  SECRET_DOOR('+', Color.TRANSPARENT),
  SECRET_CORRIDOR('#', Color.TRANSPARENT),
  POOL('~', Color.BLUE_BRIGHT),
  MOAT('M', Color.BLUE),
  WATER('~', Color.BLUE),
  DRAWBRIDGE_UP('#', Color.BROWN),
  LAVA_POOL('~', Color.RED),
  IRON_BARS('#', Color.BLUE),
  DOOR('+', Color.BROWN),
  CORRIDOR('#', Color.GRAY),
  ROOM('.', Color.GRAY),
  STAIRS('=', Color.MAGENTA),
  LADDER('=', Color.BROWN),
  FOUNTAIN('{', Color.BLUE),
  THRONE('\\', Color.YELLOW),
  SINK('s', Color.WHITE),
  GRAVE('|', Color.WHITE),
  ALTAR('_', Color.WHITE),
  ICE('.', Color.BLUE_BRIGHT),
  DRAWBRIDGE_DOWN('=', Color.GREEN_BRIGHT),
  AIR('A', Color.BLUE_BRIGHT),
  CLOUD('#', Color.WHITE),
  NOTHING('?', Color.WHITE);

  public final char symbol;
  public final Color color;

  TileType(char symbol, Color color) {
    this.symbol = symbol;
    this.color = color;
  }

  public static TileType fromValue(int value) {
    assert value >= 0 && value <= 36 : "Other tile types are not known";
    // There are many types of walls
    if (value >= 1 && value <= 12) {
      return WALL;
    }

    return switch (value) {
      case 0 -> STONE;
      case 13 -> TREE;
      case 14 -> SECRET_DOOR;
      case 15 -> SECRET_CORRIDOR;
      case 16 -> POOL;
      case 17 -> MOAT;
      case 18 -> WATER;
      case 19 -> DRAWBRIDGE_UP;
      case 20 -> LAVA_POOL;
      case 21 -> IRON_BARS;
      case 22 -> DOOR;
      case 23 -> CORRIDOR;
      case 24 -> ROOM;
      case 25 -> STAIRS;
      case 26 -> LADDER;
      case 27 -> FOUNTAIN;
      case 28 -> THRONE;
      case 29 -> SINK;
      case 30 -> GRAVE;
      case 31 -> ALTAR;
      case 32 -> ICE;
      case 33 -> DRAWBRIDGE_DOWN;
      case 34 -> AIR;
      case 35 -> CLOUD;
      case 36 -> NOTHING;
      default -> throw new IllegalArgumentException("Invalid value for TileType");
    };
  }
}
