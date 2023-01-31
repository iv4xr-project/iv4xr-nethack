//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp;

import HPASharp.Graph.ConcreteNode;
import HPASharp.Infrastructure.Id;

public class ConcretePathNode implements IPathNode {
  public ConcretePathNode() {}

  public Id<ConcreteNode> Id;

  public ConcretePathNode(Id<ConcreteNode> id) throws Exception {
    Id = id;
  }

  public int getIdValue() throws Exception {
    return Id.getIdValue();
  }
}
