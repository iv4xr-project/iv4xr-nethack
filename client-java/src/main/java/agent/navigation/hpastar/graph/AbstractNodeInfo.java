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
    StringBuilder sb = new StringBuilder();

    sb.append("id: ").append(id).append(System.lineSeparator());
    sb.append("; level: ").append(level).append(System.lineSeparator());
    sb.append("; cluster: ").append(clusterId).append(System.lineSeparator());
    sb.append("; row: ").append(position.y).append(System.lineSeparator());
    sb.append("; col: ").append(position.x).append(System.lineSeparator());
    sb.append("; center: ").append(concreteNodeId).append(System.lineSeparator());

    System.out.println(sb.toString());
  }
}
