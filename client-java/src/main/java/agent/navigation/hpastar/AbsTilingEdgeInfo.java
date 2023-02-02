//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

// implements edges in the abstract graph
public class AbsTilingEdgeInfo {
  public int cost;
  public int level;
  public boolean isInterEdge;

  public AbsTilingEdgeInfo(int cost, int level, boolean inter) {
    this.cost = cost;
    this.level = level;
    this.isInterEdge = inter;
  }

  public String toString() {
    try {
      return ("cost: " + cost + "; level: " + level + "; inter: " + isInterEdge);
    } catch (RuntimeException __dummyCatchVar0) {
      throw __dummyCatchVar0;
    } catch (Exception __dummyCatchVar0) {
      throw new RuntimeException(__dummyCatchVar0);
    }
  }

  public void printInfo() {
    System.out.println(this);
  }
}
