//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

import util.CustomVec2D;

public class Helpers {
  public static int getMaxEdges(NavType navType) {
    switch (navType) {
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

  public static boolean areAligned(CustomVec2D p1, CustomVec2D p2) {
    return p1.x == p2.x || p1.y == p2.y;
  }
}
