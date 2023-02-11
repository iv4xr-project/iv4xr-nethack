//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

import agent.navigation.hpastar.utils.RefSupport;
import eu.iv4xr.framework.spatial.IntVec2D;

public interface IPassability {
  /** Tells whether for a given position this passability class can enter or not. */
  public void updateCanMoveDiagonally(IntVec2D pos, boolean canMoveDiagonally);

  public void updateObstacle(IntVec2D pos, boolean isObstacle);

  boolean cannotEnter(IntVec2D pos, RefSupport<Integer> movementCost);

  boolean canMoveDiagonal(IntVec2D pos1, IntVec2D pos2);

  boolean canMoveDiagonal(IntVec2D pos);

  ConcreteMap slice(int horizOrigin, int vertOrigin, Size size);

  ConcreteMap getConcreteMap();
}
