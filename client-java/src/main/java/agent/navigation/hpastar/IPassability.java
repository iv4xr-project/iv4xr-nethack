//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

import CS2JNet.JavaSupport.language.RefSupport;
import agent.navigation.hpastar.utils.RefSupport;
import eu.iv4xr.framework.spatial.IntVec2D;

public interface IPassability {
  /** Tells whether for a given position this passability class can enter or not. */
  boolean canEnter(IntVec2D pos, RefSupport<Integer> movementCost);
}
