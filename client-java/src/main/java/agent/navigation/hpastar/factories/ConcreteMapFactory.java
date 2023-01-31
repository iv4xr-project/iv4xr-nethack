//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package agent.navigation.hpastar.factories;

import agent.navigation.hpastar.ConcreteMap;
import agent.navigation.hpastar.IPassability;
import agent.navigation.hpastar.TileType;

/** Constructs ConcreteMap objects */
public class ConcreteMapFactory {
  public static ConcreteMap createConcreteMap(
      int width, int height, IPassability passability, TileType tilingType) {
    return new ConcreteMap(tilingType, width, height, passability);
  }
}
