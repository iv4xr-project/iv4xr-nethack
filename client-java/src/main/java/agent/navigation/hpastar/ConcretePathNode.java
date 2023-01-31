//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

import agent.navigation.hpastar.graph.ConcreteNode;
import agent.navigation.hpastar.infrastructure.Id;

public class ConcretePathNode implements IPathNode {
  public Id<ConcreteNode> id;

  public ConcretePathNode(Id<ConcreteNode> id) {
    this.id = id;
  }

  public int getIdValue() {
    return id.getIdValue();
  }
}
