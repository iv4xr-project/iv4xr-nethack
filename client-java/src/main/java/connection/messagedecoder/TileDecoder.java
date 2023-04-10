package connection.messagedecoder;

import agent.navigation.surface.Tile;
import java.io.DataInputStream;
import java.io.IOException;
import nethack.world.Level;
import nethack.world.tiles.*;
import util.CustomVec2D;
import util.CustomVec3D;

public class TileDecoder extends Decoder {
  public static Tile[][] decode(DataInputStream input) throws IOException {
    Tile[][] tiles = new Tile[Level.SIZE.height][Level.SIZE.width];

    int bytesPerTile = 5;
    short nrTiles = input.readShort();
    byte[] tilesData = input.readNBytes(bytesPerTile * nrTiles);
    for (int i = 0, offset = 0; i < nrTiles; i++, offset += bytesPerTile) {
      byte x = tilesData[offset];
      byte y = tilesData[offset + 1];
      byte tile = tilesData[offset + 2];
      byte flags = tilesData[offset + 3];
      boolean visible = parseBool(tilesData[offset + 4]);
      tiles[y][x] = toTile(x, y, tile, flags, visible);
    }

    return tiles;
  }

  // A definition of tiles and their color is in lib/nle/src/drawing.c
  private static Tile toTile(int x, int y, byte tileType, byte flags, boolean visible) {
    assert tileType >= 0 && tileType <= 36
        : String.format("TileType value must be >= 0 && <= 35 but value=%d", tileType);
    CustomVec3D loc = new CustomVec3D(0, new CustomVec2D(x, y));

    Tile tile = null;
    // Do not differentiate between all wall types
    if (tileType >= 1 && tileType <= 12) {
      tile = new Wall(loc);
    } else if (tileType == 36) {
      return null;
    }

    switch (tileType) {
      case 0: // Stone
        return null;
      case 13:
        tile = new Tree(loc);
        break;
      case 14: // Secret door
      case 22: // Door
        boolean broken = (flags & 1) != 0;
        boolean isOpen = (flags & 2) != 0;
        boolean closed = (flags & 4) != 0;
        boolean locked = (flags & 8) != 0;
        boolean trapped = (flags & 16) != 0;
        Door door = new Door(loc, broken, isOpen, closed, locked, trapped);
        if (tileType == 14) {
          door.setSecret();
        }
        tile = door;
        break;
      case 15: // Secret corridor
      case 23: // Corridor
        Corridor corridor = new Corridor(loc);
        if (tileType == 15) {
          corridor.setSecret();
        }
        tile = corridor;
        break;
      case 16: // Pool
        tile = new Pool(loc);
        break;
      case 17: // Moat
        tile = new Moat(loc);
        break;
      case 18:
        tile = new Water(loc);
        break;
      case 19: // Drawbridge up
        return null;
      case 20:
        tile = new Lava(loc);
        break;
      case 21:
        tile = new IronBars(loc);
        break;
      case 24:
        tile = new Floor(loc);
        break;
      case 25: // Stair
      case 26: // Ladder
        assert flags == 1 || flags == 2;
        tile = new Stair(loc, flags == 1);
        break;
      case 27:
        tile = new Fountain(loc);
        break;
      case 28:
        tile = new Throne(loc);
        break;
      case 29:
        tile = new Sink(loc);
        break;
      case 30: // Grave
        tile = new Grave(loc);
        break;
      case 31: // Altar
        tile = new Altar(loc);
        break;
      case 32: // Ice
        tile = new Ice(loc);
        break;
      case 33: // Drawbridge down
        return null;
      case 34: // Air
        tile = new Air(loc);
        break;
      case 35: // Cloud
        tile = new Cloud(loc);
        break;
    }

    assert tile != null && tile instanceof Viewable;
    ((Viewable) tile).setVisibility(visible);
    return tile;
  }
}
