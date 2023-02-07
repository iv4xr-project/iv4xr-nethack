//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.graph;

import agent.navigation.hpastar.infrastructure.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import nl.uu.cs.aplib.utils.Pair;

/**
 * A graph is a set of nodes connected with edges. Each node and edge can hold a certain amount of
 * information, which is expressed in the templated parameters NODEINFO and EDGEINFO
 */
public class Graph<
    TNode extends INode<TNode, TNodeInfo, TEdge>,
    TNodeInfo,
    TEdge extends IEdge<TNode, TEdgeInfo>,
    TEdgeInfo> {
  // We store the nodes in a list because the main operations we use
  // in this list are additions, random accesses and very few removals (only when
  // adding or removing nodes to perform specific searches).
  // This list is implicitly indexed by the nodeId, which makes removing a random
  // Node in the list quite of a mess. We could use a dictionary to ease removals,
  // but lists and arrays are faster for random accesses, and we need performance.
  public List<TNode> nodes = new ArrayList<>();
  private final Function<Pair<Id<TNode>, TNodeInfo>, TNode> _nodeCreator;
  private final Function<Pair<Id<TNode>, TEdgeInfo>, TEdge> _edgeCreator;

  public Graph(
      Function<Pair<Id<TNode>, TNodeInfo>, TNode> nodeCreator,
      Function<Pair<Id<TNode>, TEdgeInfo>, TEdge> edgeCreator) {
    _nodeCreator = nodeCreator;
    _edgeCreator = edgeCreator;
  }

  /**
   * Adds or updates a node with the provided info. A node is updated only if the nodeId provided
   * previously existed.
   */
  public void addNode(Id<TNode> nodeId, TNodeInfo info) {
    int size = nodeId.getIdValue() + 1;
    if (nodes.size() < size) {
      nodes.add(_nodeCreator.apply(new Pair<>(nodeId, info)));
    } else {
      nodes.set(nodeId.getIdValue(), _nodeCreator.apply(new Pair<>(nodeId, info)));
    }
  }

  public void addEdge(Id<TNode> sourceNodeId, Id<TNode> targetNodeId, TEdgeInfo info) {
    nodes
        .get(sourceNodeId.getIdValue())
        .addEdge(_edgeCreator.apply(new Pair<>(targetNodeId, info)));
  }

  public void removeEdgesFromAndToNode(Id<TNode> nodeId) {
    List<Id<TNode>> keys = new ArrayList<>(nodes.get(nodeId.getIdValue()).edges.keySet());
    for (Id<TNode> targetNodeId : keys) {
      nodes.get(targetNodeId.getIdValue()).removeEdge(nodeId);
    }
    nodes.get(nodeId.getIdValue()).edges.clear();
  }

  public void removeLastNode() {
    nodes.remove(nodes.size() - 1);
  }

  public TNode getNode(Id<TNode> nodeId) {
    //    System.out.println(nodeId.getIdValue());
    if (nodeId.getIdValue() < 0) {
      System.out.println("TRACE");
    }
    return nodes.get(nodeId.getIdValue());
  }

  public TNodeInfo getNodeInfo(Id<TNode> nodeId) {
    return getNode(nodeId).info;
  }

  public Map<Id<TNode>, TEdge> getEdges(Id<TNode> nodeId) {
    return nodes.get(nodeId.getIdValue()).edges;
  }
}
