package agent.navigation.hpastar.search;

import agent.navigation.hpastar.infrastructure.Id;
import java.util.List;

public class Path<TNode> {
  public final int pathCost;
  public final List<Id<TNode>> pathNodes;

  public Path(List<Id<TNode>> pathNodes, int pathCost) {
    this.pathCost = pathCost;
    this.pathNodes = pathNodes;
  }
}
