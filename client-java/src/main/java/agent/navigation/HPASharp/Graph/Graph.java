//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Graph;

import HPASharp.Graph.IEdge;
import HPASharp.Graph.INode;
import HPASharp.Infrastructure.Id;

/**
* A graph is a set of nodes connected with edges. Each node and edge can hold
* a certain amount of information, which is expressed in the templated parameters
* NODEINFO and EDGEINFO
*/
public class Graph <TNode extends INode<TNode,TNodeInfo,TEdge>, TNodeInfo, TEdge extends IEdge<TNode,TEdgeInfo>, TEdgeInfo>
{
    // We store the nodes in a list because the main operations we use
    // in this list are additions, random accesses and very few removals (only when
    // adding or removing nodes to perform specific searches).
    // This list is implicitly indexed by the nodeId, which makes removing a random
    // Node in the list quite of a mess. We could use a dictionary to ease removals,
    // but lists and arrays are faster for random accesses, and we need performance.
    private List<TNode> __Nodes = new List<TNode>();
    public List<TNode> getNodes() {
        return __Nodes;
    }

    public void setNodes(List<TNode> value) {
        __Nodes = value;
    }

    private final Func<Id<TNode>, TNodeInfo, TNode> _nodeCreator = new Func<Id<TNode>, TNodeInfo, TNode>();
    private final Func<Id<TNode>, TEdgeInfo, TEdge> _edgeCreator = new Func<Id<TNode>, TEdgeInfo, TEdge>();
    public Graph(Func<Id<TNode>, TNodeInfo, TNode> nodeCreator, Func<Id<TNode>, TEdgeInfo, TEdge> edgeCreator) throws Exception {
        setNodes(new List<TNode>());
        _nodeCreator = nodeCreator;
        _edgeCreator = edgeCreator;
    }

    /**
    * Adds or updates a node with the provided info. A node is updated
    * only if the nodeId provided previously existed.
    */
    public void addNode(Id<TNode> nodeId, TNodeInfo info) throws Exception {
        /* [UNSUPPORTED] 'var' as type is unsupported "var" */ size = nodeId.getIdValue() + 1;
        if (getNodes().Count < size)
            getNodes().Add(_nodeCreator(nodeId, info));
        else
            getNodes()[nodeId.getIdValue()] = _nodeCreator(nodeId, info);
    }

    public void addEdge(Id<TNode> sourceNodeId, Id<TNode> targetNodeId, TEdgeInfo info) throws Exception {
        getNodes()[sourceNodeId.getIdValue()].AddEdge(_edgeCreator(targetNodeId, info));
    }

    public void removeEdgesFromAndToNode(Id<TNode> nodeId) throws Exception {
        for (/* [UNSUPPORTED] 'var' as type is unsupported "var" */ targetNodeId : getNodes()[nodeId.getIdValue()].Edges.Keys)
        {
            getNodes()[targetNodeId.IdValue].RemoveEdge(nodeId);
        }
        getNodes()[nodeId.getIdValue()].Edges.Clear();
    }

    public void removeLastNode() throws Exception {
        getNodes().RemoveAt(getNodes().Count - 1);
    }

    public TNode getNode(Id<TNode> nodeId) throws Exception {
        return getNodes()[nodeId.getIdValue()];
    }

    public TNodeInfo getNodeInfo(Id<TNode> nodeId) throws Exception {
        return getNode(nodeId).Info;
    }

    public IDictionary<Id<TNode>, TEdge> getEdges(Id<TNode> nodeId) throws Exception {
        return getNodes()[nodeId.getIdValue()].Edges;
    }

}
