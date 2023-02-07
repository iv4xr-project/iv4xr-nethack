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

    assert relativePosition.x >= 0 && relativePosition.y >= 0
        : "Relative position should not be negative";
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof EntrancePoint)) {
      return false;
    }
    EntrancePoint otherEntrance = (EntrancePoint) other;
    return abstractNodeId.equals(otherEntrance.abstractNodeId)
        && relativePosition.equals(otherEntrance.relativePosition);
  }
}
