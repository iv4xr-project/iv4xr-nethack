//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:33
//

package HPASharp;

import HPASharp.Graph.AbstractNode;
import HPASharp.Infrastructure.Id;

/**
 * An Entrance Point represents a point inside a cluster that belongs to an entrance. It holds a
 * reference to the abstract node it belongs to
 */
public class EntrancePoint {
  private Id<AbstractNode> __AbstractNodeId;

  public Id<AbstractNode> getAbstractNodeId() {
    return __AbstractNodeId;
  }

  public void setAbstractNodeId(Id<AbstractNode> value) {
    __AbstractNodeId = value;
  }

  private Position __RelativePosition = new Position();

  public Position getRelativePosition() {
    return __RelativePosition;
  }

  public void setRelativePosition(Position value) {
    __RelativePosition = value;
  }

  public EntrancePoint(Id<AbstractNode> abstractNodeId, Position relativePosition)
      throws Exception {
    setAbstractNodeId(abstractNodeId);
    setRelativePosition(relativePosition);
  }
}
