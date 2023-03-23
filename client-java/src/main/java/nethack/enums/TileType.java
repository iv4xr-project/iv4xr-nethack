package nethack.enums;

public enum TileType {
  STONE,
  WALL,
  TREE,
  SECRET_DOOR,
  SECRET_CORRIDOR,
  POOL,
  MOAT,
  WATER,
  DRAWBRIDGE_UP,
  LAVA_POOL,
  IRON_BARS,
  DOOR,
  CORRIDOR,
  ROOM,
  STAIRS,
  LADDER,
  FOUNTAIN,
  THRONE,
  SINK,
  GRAVE,
  ALTAR,
  ICE,
  DRAWBRIDGE_DOWN,
  AIR,
  CLOUD,
  NOTHING;

  public static TileType fromValue(int value) {
    assert value >= 0 && value <= 36 : "Other tile types are not known";
    // There are many types of walls
    if (value >= 1 && value <= 12) {
      return WALL;
    }

    switch (value) {
      case 0:
        return STONE;
      case 13:
        return TREE;
      case 14:
        return SECRET_DOOR;
      case 15:
        return SECRET_CORRIDOR;
      case 16:
        return POOL;
      case 17:
        return MOAT;
      case 18:
        return WATER;
      case 19:
        return DRAWBRIDGE_UP;
      case 20:
        return LAVA_POOL;
      case 21:
        return IRON_BARS;
      case 22:
        return DOOR;
      case 23:
        return CORRIDOR;
      case 24:
        return ROOM;
      case 25:
        return STAIRS;
      case 26:
        return LADDER;
      case 27:
        return FOUNTAIN;
      case 28:
        return THRONE;
      case 29:
        return SINK;
      case 30:
        return GRAVE;
      case 31:
        return ALTAR;
      case 32:
        return ICE;
      case 33:
        return DRAWBRIDGE_DOWN;
      case 34:
        return AIR;
      case 35:
        return CLOUD;
      case 36:
        return NOTHING;
      default:
        throw new IllegalArgumentException("Invalid value for TileType");
    }
  }
}
