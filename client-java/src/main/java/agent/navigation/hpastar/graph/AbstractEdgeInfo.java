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
    return ("cost: " + cost + "; level: " + level + "; interCluster: " + isInterClusterEdge);
  }
}
