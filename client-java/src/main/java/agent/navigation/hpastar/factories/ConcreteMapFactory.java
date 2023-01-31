//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package HPASharp.Factories;

import HPASharp.ConcreteMap;
import HPASharp.IPassability;
import HPASharp.TileType;

/** Constructs ConcreteMap objects */
public class ConcreteMapFactory {
  public static ConcreteMap createConcreteMap(
      int width, int height, IPassability passability, TileType tilingType) throws Exception {
    ConcreteMap tiling = new ConcreteMap(tilingType, width, height, passability);
    return tiling;
  }
}
