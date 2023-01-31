//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Infrastructure;

import HPASharp.Connection;

/** Interface to search environment. */
public interface IMap<TNode> {
  int getNrNodes() throws Exception;

  /**
   * Generate successor nodes for the search.
   *
   * @param lastNodeId Can be used to prune nodes, (is set to NO_NODE in Search::checkPathExists).
   */
  IEnumerable<Connection<TNode>> getConnections(Id<TNode> nodeId) throws Exception;

  int getHeuristic(Id<TNode> startNodeId, Id<TNode> targetNodeId) throws Exception;
}
