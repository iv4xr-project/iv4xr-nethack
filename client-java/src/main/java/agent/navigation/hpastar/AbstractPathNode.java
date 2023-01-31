//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

import agent.navigation.hpastar.graph.AbstractNode;
import agent.navigation.hpastar.infrastructure.Id;

public class AbstractPathNode implements IPathNode {
  public Id<AbstractNode> id;
  public int level;

  public AbstractPathNode(Id<AbstractNode> id, int lvl) {
    this.id = id;
    level = lvl;
  }

  public int getIdValue() {
    return id.getIdValue();
  }
}
