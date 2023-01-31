//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.graph;

import agent.navigation.hpastar.infrastructure.Id;
import java.util.ArrayList;
import java.util.List;

public class AbstractEdgeInfo {
  public int cost;
  public int level;
  public boolean isInterClusterEdge;
  public List<Id<AbstractNode>> innerLowerLevelPath = new ArrayList<>();

  public AbstractEdgeInfo(int cost, int level, boolean interCluster) {
    this.cost = cost;
    this.level = level;
    this.isInterClusterEdge = interCluster;
  }

  public String toString() {
    try {
      return ("cost: " + cost + "; level: " + level + "; interCluster: " + isInterClusterEdge);
    } catch (RuntimeException __dummyCatchVar0) {
      throw __dummyCatchVar0;
    } catch (Exception __dummyCatchVar0) {
      throw new RuntimeException(__dummyCatchVar0);
    }
  }

  public void printInfo() {
    System.out.println(this.toString());
  }
}
