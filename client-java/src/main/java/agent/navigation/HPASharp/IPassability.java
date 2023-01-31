//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp;

import CS2JNet.JavaSupport.language.RefSupport;

public interface IPassability {
  /** Tells whether for a given position this passability class can enter or not. */
  boolean canEnter(Position pos, RefSupport<int> movementCost) throws Exception;
}
