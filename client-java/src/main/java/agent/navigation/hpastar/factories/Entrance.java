//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package agent.navigation.hpastar.factories;

import agent.navigation.hpastar.Cluster;
import agent.navigation.hpastar.Orientation;
import agent.navigation.hpastar.graph.ConcreteNode;
import agent.navigation.hpastar.infrastructure.Id;

public class Entrance {
  public Id<Entrance> id;
  public Cluster cluster1;
  public Cluster cluster2;
  public ConcreteNode srcNode;
  public ConcreteNode destNode;
  public Orientation orientation = Orientation.Horizontal;

  public Entrance(
      Id<Entrance> id,
      Cluster cluster1,
      Cluster cluster2,
      ConcreteNode srcNode,
      ConcreteNode destNode,
      Orientation orientation) {
    this.id = id;
    this.cluster1 = cluster1;
    this.cluster2 = cluster2;
    this.srcNode = srcNode;
    this.destNode = destNode;
    this.orientation = orientation;
  }

  public int getEntranceLevel(int clusterSize, int maxLevel) {
    assert orientation == Orientation.Horizontal || orientation == Orientation.Vertical;
    if (orientation.equals(Orientation.Horizontal)) {
      return determineLevel(clusterSize, maxLevel, srcNode.info.position.x);
    } else {
      return determineLevel(clusterSize, maxLevel, srcNode.info.position.y);
    }
  }

  private int determineLevel(int clusterSize, int maxLevel, int y) {
    int level = 1;
    if (y % clusterSize != 0) {
      y++;
    }

    if (y < clusterSize) {
      return 1;
    }

    int clusterY = y / clusterSize;
    while (clusterY % 2 == 0 && level < maxLevel) {
      clusterY /= 2;
      level++;
    }
    return Math.min(level, maxLevel);
  }

  @Override
  public String toString() {
    return String.format("Entrance %d %s %s %n", id.getIdValue(), cluster1, cluster2);
  }
}
