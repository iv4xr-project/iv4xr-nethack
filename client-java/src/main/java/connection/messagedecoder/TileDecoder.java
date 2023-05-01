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
      case 0 -> { // Stone
        return null;
      }
      case 13 -> tile = new Tree(loc);
        // Secret door
      case 14, 22 -> { // Door
        boolean broken = (flags & 1) != 0;
        boolean isOpen = (flags & 2) != 0;
        boolean closed = (flags & 4) != 0;
        boolean locked = (flags & 8) != 0;
        boolean trapped = (flags & 16) != 0;
        Door door = new Door(loc, broken, isOpen, closed, locked, trapped, flags);
        if (tileType == 14) {
          door.setSecret();
        }
        tile = door;
      } // Secret corridor
      case 15, 23 -> { // Corridor
        Corridor corridor = new Corridor(loc);
        if (tileType == 15) {
          corridor.setSecret();
        }
        tile = corridor;
      }
      case 16 -> // Pool
      tile = new Pool(loc);
      case 17 -> // Moat
      tile = new Moat(loc);
      case 18 -> tile = new Water(loc);
      case 19 -> { // Drawbridge up
        return null;
      }
      case 20 -> tile = new Lava(loc);
      case 21 -> tile = new IronBars(loc);
      case 24 -> tile = new Floor(loc);
        // Stair
      case 25, 26 -> { // Ladder
        assert flags == 1 || flags == 2;
        tile = new Stair(loc, flags == 1);
      }
      case 27 -> tile = new Fountain(loc);
      case 28 -> tile = new Throne(loc);
      case 29 -> tile = new Sink(loc);
      case 30 -> // Grave
      tile = new Grave(loc);
      case 31 -> // Altar
      tile = new Altar(loc);
      case 32 -> // Ice
      tile = new Ice(loc);
      case 33 -> { // Drawbridge down
        return null;
      }
      case 34 -> // Air
      tile = new Air(loc);
      case 35 -> // Cloud
      tile = new Cloud(loc);
    }

    assert tile != null && tile instanceof Viewable;
    ((Viewable) tile).setVisibility(visible);
    return tile;
  }
}
