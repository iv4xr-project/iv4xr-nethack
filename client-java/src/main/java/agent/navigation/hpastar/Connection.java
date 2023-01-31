//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

import agent.navigation.hpastar.infrastructure.Id;

public class Connection<TNode> {
  public Id<TNode> target;
  public int cost;

  public Connection(Id<TNode> target, int cost) {
    this.target = target;
    this.cost = cost;
  }
}
