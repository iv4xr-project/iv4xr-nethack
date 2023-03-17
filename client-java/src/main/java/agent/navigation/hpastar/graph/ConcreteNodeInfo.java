//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.graph;

import util.CustomVec2D;

public class ConcreteNodeInfo {
  public final CustomVec2D position;
  public boolean isObstacle;
  public int cost;

  public ConcreteNodeInfo(boolean isObstacle, int cost, CustomVec2D position) {
    this.isObstacle = isObstacle;
    this.position = position;
    this.cost = cost;
  }

  @Override
  public String toString() {
    return String.format(
        "ConcreteNodeInfo: %s (isObstacle=%b, cost=%d)", position, isObstacle, cost);
  }
}
