//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.graph;

public class ConcreteEdgeInfo {
  public final int cost;

  public ConcreteEdgeInfo(int cost) {
    this.cost = cost;
  }

  @Override
  public String toString() {
    return String.format("ConcreteEdgeInfo (cost=%d)", cost);
  }
}
