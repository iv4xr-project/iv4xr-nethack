package agent.navigation.hpastar.search;

import agent.navigation.hpastar.infrastructure.Id;

///  <summary>
///  An AStar node embeds the status of a processed node, containing information like
///  the cost it's taken to reach it (Cost So far, G), the expected cost to reach the goal
///  (The heuristic, H), the parent where this node came from (which will serve later to reconstruct
// the
// best paths)
///  the current Status of the node (Open, Closed, Unexplored, see CellStatus documentation for more
// information) and the F-score
///  that serves to compare which nodes are the best
///  </summary>
public class AStarNode<TNode> {

  public AStarNode(Id<TNode> parent, int g, int h, CellStatus status) {
    this.parent = parent;
    this.g = g;
    this.h = h;
    this.f = (g + h);
    this.status = status;
  }

  public final Id<TNode> parent;

  public final CellStatus status;

  public final int h;

  public final int g;

  public final int f;
}
