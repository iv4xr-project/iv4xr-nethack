//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

import util.CustomVec2D;

public class Helpers {
  public static int getMaxEdges(NavType navType) {
    return switch (navType) {
      case Hex -> 6;
      case Octile, OctileUnicost -> 8;
      case Tile -> 4;
    };
  }

  public static boolean areAligned(CustomVec2D p1, CustomVec2D p2) {
    return p1.x == p2.x || p1.y == p2.y;
  }
}
