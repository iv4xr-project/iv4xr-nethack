//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package agent.navigation.hpastar.factories;

import agent.navigation.hpastar.ConcreteMap;
import agent.navigation.hpastar.IPassability;
import agent.navigation.hpastar.NavType;
import agent.navigation.hpastar.Size;

/** Constructs ConcreteMap objects */
public class ConcreteMapFactory {
  public static ConcreteMap createConcreteMap(
      Size size, IPassability passability, NavType tilingType) {
    return new ConcreteMap(tilingType, size, passability);
  }
}
