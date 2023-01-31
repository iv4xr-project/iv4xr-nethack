//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Graph;

import HPASharp.Graph.AbstractEdge;
import HPASharp.Graph.AbstractEdgeInfo;
import HPASharp.Graph.AbstractNode;
import HPASharp.Graph.AbstractNodeInfo;
import HPASharp.Graph.Graph;

public class AbstractGraph  extends Graph<AbstractNode,AbstractNodeInfo,AbstractEdge,AbstractEdgeInfo>
{
    public AbstractGraph() throws Exception {
        super(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(nodeid, info) => {
            return new AbstractNode(nodeid, info);
        }" */, /* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(nodeid, info) => {
            return new AbstractEdge(nodeid, info);
        }" */);
    }

}
