package connection.messagedecoder;

import agent.navigation.surface.Tile;
import nethack.world.tiles.*;
import util.CustomVec2D;
import util.CustomVec3D;

public class TileDecoder extends Decoder {
  public static Tile decode(int x, int y, byte tileType, byte flags) {
    assert tileType >= 0 && tileType <= 35
        : String.format("TileType value must be >= 0 && <= 35 but value=%d", tileType);
    CustomVec3D loc = new CustomVec3D(0, new CustomVec2D(x, y));

    // Do not differentiate between all wall types
    if (tileType >= 1 && tileType <= 12) {
      return new Wall(loc);
    }

    switch (tileType) {
      case 0: // Stone
        return null;
      case 13:
        return new Tree(loc);
      case 14: // Secret door
      case 22: // Door
        boolean broken = (flags & 1) != 0;
        boolean isOpen = (flags & 2) != 0;
        boolean closed = (flags & 4) != 0;
        boolean locked = (flags & 8) != 0;
        boolean trapped = (flags & 16) != 0;
        if (tileType == 14) {
          return new SecretDoor(loc, broken, isOpen, closed, locked, trapped);
        } else {
          return new Door(loc, broken, isOpen, closed, locked, trapped);
        }
      case 15: // Secret corridor
        return null;
      case 16: // Pool
        return null;
      case 17: // Moat
        return null;
      case 18:
        return new Water(loc);
      case 19: // Drawbridge up
        return null;
      case 20:
        return new Lava(loc);
      case 21:
        return new IronBars(loc);
      case 23:
        return new Corridor(loc);
      case 24:
        return new Floor(loc);
      case 25: // Stair
      case 26: // Ladder
        assert flags == 1 || flags == 2;
        return new Stair(loc, flags == 1);
      case 27:
        return new Fountain(loc);
      case 28:
        return new Throne(loc);
      case 29:
        return new Sink(loc);
      case 30: // Grave
        return null;
      case 31: // Altar
        return null;
      case 32: // Ice
        return null;
      case 33: // Drawbridge down
        return null;
      case 34: // Air
        return null;
      case 35: // Cloud
        return null;
    }

    throw new IllegalArgumentException(String.format("TileType %d invalid", tileType));
  }
}
