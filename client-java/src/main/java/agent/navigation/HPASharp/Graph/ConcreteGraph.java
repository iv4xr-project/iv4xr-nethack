//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Graph;

import HPASharp.Graph.ConcreteEdge;
import HPASharp.Graph.ConcreteEdgeInfo;
import HPASharp.Graph.ConcreteNode;
import HPASharp.Graph.ConcreteNodeInfo;
import HPASharp.Graph.Graph;

public class ConcreteGraph  extends Graph<ConcreteNode,ConcreteNodeInfo,ConcreteEdge,ConcreteEdgeInfo>
{
    public ConcreteGraph() throws Exception {
        super(/* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(nodeid, info) => {
            return new ConcreteNode(nodeid, info);
        }" */, /* [UNSUPPORTED] to translate lambda expressions we need an explicit delegate type, try adding a cast "(nodeid, info) => {
            return new ConcreteEdge(nodeid, info);
        }" */);
    }

}
