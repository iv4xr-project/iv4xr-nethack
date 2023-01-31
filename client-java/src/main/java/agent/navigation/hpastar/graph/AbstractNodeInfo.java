//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.graph;

import agent.navigation.hpastar.Cluster;
import agent.navigation.hpastar.infrastructure.Id;
import eu.iv4xr.framework.spatial.IntVec2D;
import org.apache.commons.lang.NotImplementedException;

// implements nodes in the abstract graph
public class AbstractNodeInfo {
  public Id<AbstractNode> id;
  public IntVec2D position;
  public Id<Cluster> clusterId;
  public Id<ConcreteNode> concreteNodeId;
  public int level;

  public AbstractNodeInfo(
      Id<AbstractNode> id,
      int level,
      Id<Cluster> clId,
      IntVec2D position,
      Id<ConcreteNode> concreteNodeId) {
    this.id = id;
    this.level = level;
    this.clusterId = clId;
    this.position = position;
    this.concreteNodeId = concreteNodeId;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof AbstractNodeInfo)) {
      return false;
    }

    throw new NotImplementedException("Equals not implemented yet");
    //        return super.equals(obj);
  }

  public void printInfo() {
    System.out.println("id: " + id);
    System.out.println("; level: " + level);
    System.out.println("; cluster: " + clusterId);
    System.out.println("; row: " + position.y);
    System.out.println("; col: " + position.x);
    System.out.println("; center: " + concreteNodeId);
    System.out.println();
  }
}
