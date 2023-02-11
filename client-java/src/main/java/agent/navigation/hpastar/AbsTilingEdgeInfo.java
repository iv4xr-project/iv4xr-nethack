//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

// implements edges in the abstract graph
public class AbsTilingEdgeInfo {
  public final int cost;
  public final int level;
  public final boolean isInterEdge;

  public AbsTilingEdgeInfo(int cost, int level, boolean inter) {
    this.cost = cost;
    this.level = level;
    this.isInterEdge = inter;
  }

  public String toString() {
    return ("cost: " + cost + "; level: " + level + "; inter: " + isInterEdge);
  }
}
