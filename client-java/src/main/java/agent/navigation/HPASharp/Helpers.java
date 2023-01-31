//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp;


public class Helpers {
  public static int getMaxEdges(TileType tileType) throws Exception {
    switch (tileType) {
      case Hex:
        return 6;
      case Octile:
      case OctileUnicost:
        return 8;
      case Tile:
        return 4;
    }
    return 0;
  }

  public static boolean areAligned(Position p1, Position p2) throws Exception {
    return p1.X == p2.X || p1.Y == p2.Y;
  }
}
