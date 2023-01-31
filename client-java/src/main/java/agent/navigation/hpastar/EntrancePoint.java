//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package agent.navigation.hpastar;

import agent.navigation.hpastar.graph.AbstractNode;
import agent.navigation.hpastar.infrastructure.Id;
import eu.iv4xr.framework.spatial.IntVec2D;

/**
 * An Entrance Point represents a point inside a cluster that belongs to an entrance. It holds a
 * reference to the abstract node it belongs to
 */
public class EntrancePoint {
  public Id<AbstractNode> abstractNodeId;
  public IntVec2D relativePosition;

  public EntrancePoint(Id<AbstractNode> abstractNodeId, IntVec2D relativePosition) {
    this.abstractNodeId = abstractNodeId;
    this.relativePosition = relativePosition;
  }
}
