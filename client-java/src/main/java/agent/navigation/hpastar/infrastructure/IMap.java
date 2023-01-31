//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.infrastructure;

import agent.navigation.hpastar.Connection;

/** Interface to search environment. */
public interface IMap<TNode> {
  int getNrNodes();

  /**
   * Generate successor nodes for the search.
   *
   * @param lastNodeId Can be used to prune nodes, (is set to NO_NODE in Search::checkPathExists).
   */
  Iterable<Connection<TNode>> getConnections(Id<TNode> nodeId);

  int getHeuristic(Id<TNode> startNodeId, Id<TNode> targetNodeId);
}
