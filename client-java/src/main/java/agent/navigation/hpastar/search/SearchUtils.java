//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:35
//

package agent.navigation.hpastar.search;

import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.infrastructure.IMap;

public class SearchUtils {
  IMap m_env;
  boolean[] closedList;
  int m_target;

  public boolean checkPathExists(IMap env, int start, int target) {
    m_env = env;
    m_target = target;
    closedList = new boolean[env.getNrNodes()];
    return searchPathExists(start, 0);
  }

  List<List<Neighbour>> m_successorStack = new ArrayList<ArrayList<Neighbour>>();

  private boolean searchPathExists(int node, int depth) {
    // AdiB 21/04/2003
    if (depth > 10000) return false;

    if (this.closedList[node]) return false;

    if (node == m_target) return true;

    this.closedList[node] = true;
    if (m_successorStack.size() < depth + 1) m_successorStack.add(new ArrayList<Neighbour>());

    m_successorStack[depth] = m_env.getNeighbours(node, Constants.NO_NODE);
    int numberSuccessors = m_successorStack[depth].Count;
    for (int i = 0; i < numberSuccessors; ++i) {
      // Get reference on successor again, because resize could have
      // changed it.
      Neighbour successor = m_successorStack.get(depth).get(i);
      int targetNodeId = successor.target;
      if (searchPathExists(targetNodeId, depth + 1)) {
        return true;
      }
    }
    return false;
  }
}
