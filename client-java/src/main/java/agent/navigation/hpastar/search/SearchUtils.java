//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:35
//

package HPASharp.Search;


public class SearchUtils
{
    IMap m_env = new IMap();
    boolean[] closedList = new boolean[]();
    int m_target = new int();
    public boolean checkPathExists(IMap env, int start, int target) throws Exception {
        m_env = env;
        m_target = target;
        closedList = new boolean[env.NrNodes];
        return searchPathExists(start,0);
    }

    List<List<Neighbour>> m_successorStack = new List<List<Neighbour>>();
    private boolean searchPathExists(int node, int depth) throws Exception {
        // AdiB 21/04/2003
        if (depth > 10000)
            return false;

        if (this.closedList[node])
            return false;

        if (node == m_target)
            return true;

        this.closedList[node] = true;
        if (m_successorStack.Count < depth + 1)
            m_successorStack.Add(new List<Neighbour>());

        m_successorStack[depth] = m_env.GetNeighbours(node, Constants.NO_NODE);
        int numberSuccessors = m_successorStack[depth].Count;
        for (int i = 0;i < numberSuccessors;++i)
        {
            // Get reference on successor again, because resize could have
            // changed it.
            /* [UNSUPPORTED] 'var' as type is unsupported "var" */ successor = m_successorStack[depth][i];
            int targetNodeId = successor.Target;
            if (searchPathExists(targetNodeId,depth + 1))
                return true;

        }
        return false;
    }

}
