//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.graph;

import agent.navigation.hpastar.Cluster;
import agent.navigation.hpastar.infrastructure.Id;
import eu.iv4xr.framework.spatial.IntVec2D;
import org.apache.commons.lang.NotImplementedException;
import util.ColoredStringBuilder;

// implements nodes in the abstract graph
public class AbstractNodeInfo {
  public final Id<AbstractNode> id;
  public final IntVec2D position;
  public final Id<Cluster> clusterId;
  public final Id<ConcreteNode> concreteNodeId;
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
    ColoredStringBuilder csb = new ColoredStringBuilder();

    csb.appendf("id: %d%n", id);
    csb.appendf("; level: %d%n", level);
    csb.appendf("; cluster: %d%n", clusterId);
    csb.appendf("; row: %d%n", position.y);
    csb.appendf("; col: %d%n", position.x);
    csb.appendf("; center: %d%n", concreteNodeId);

    System.out.println(csb);
  }
}
