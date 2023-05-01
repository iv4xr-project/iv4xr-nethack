//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

import agent.navigation.hpastar.utils.RefSupport;
import util.CustomVec2D;

public interface IPassability {
  /** Tells whether for a given position this passability class can enter or not. */
  void updateCanMoveDiagonally(CustomVec2D pos, boolean canMoveDiagonally);

  void updateObstacle(CustomVec2D pos, boolean isObstacle);

  boolean cannotEnter(CustomVec2D pos, RefSupport<Integer> movementCost);

  boolean cannotEnter(CustomVec2D pos);

  boolean canMoveDiagonal(CustomVec2D pos1, CustomVec2D pos2);

  boolean canMoveDiagonal(CustomVec2D pos);

  ConcreteMap slice(int horizOrigin, int vertOrigin, Size size);

  ConcreteMap getConcreteMap();
}
