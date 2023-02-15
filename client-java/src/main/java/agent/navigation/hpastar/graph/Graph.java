//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar.graph;
;
import agent.navigation.hpastar.infrastructure.Id;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.Logger;
import util.Loggers;

/**
 * A graph is a set of nodes connected with edges. Each node and edge can hold a certain amount of
 * information, which is expressed in the templated parameters NODEINFO and EDGEINFO
 */
public class Graph<
    TNode extends INode<TNode, TNodeInfo, TEdge>,
    TNodeInfo,
    TEdge extends IEdge<TNode, TEdgeInfo>,
    TEdgeInfo> {
  static final Logger hpaLogger = Loggers.HPALogger;
  public int nextId = 0;
  // We store the nodes in a list because the main operations we use
  // in this list are additions, random accesses and very few removals (only when
  // adding or removing nodes to perform specific searches).
  // This list is implicitly indexed by the nodeId, which makes removing a random
  // Node in the list quite of a mess. We could use a dictionary to ease removals,
  // but lists and arrays are faster for random accesses, and we need performance.
  private final Map<Id<TNode>, TNode> nodes = new HashMap<>();
  private final Function<Pair<Id<TNode>, TNodeInfo>, TNode> nodeCreator;
  private final Function<Pair<Id<TNode>, TEdgeInfo>, TEdge> edgeCreator;

  public Graph(
      Function<Pair<Id<TNode>, TNodeInfo>, TNode> nodeCreator,
      Function<Pair<Id<TNode>, TEdgeInfo>, TEdge> edgeCreator) {
    this.nodeCreator = nodeCreator;
    this.edgeCreator = edgeCreator;
  }

  /**
   * Adds or updates a node with the provided info. A node is updated only if the nodeId provided
   * previously existed.
   */
  public void addNode(Id<TNode> nodeId, TNodeInfo info) {
    if (!nodes.containsKey(nodeId)) {
      nextId++;
    }
    nodes.put(nodeId, nodeCreator.apply(new Pair<>(nodeId, info)));
  }

  public void addEdge(Id<TNode> sourceNodeId, Id<TNode> targetNodeId, TEdgeInfo info) {
    hpaLogger.debug("AddEdge: %s -> %s", getNodeInfo(sourceNodeId), getNodeInfo(targetNodeId));
    TNode node = nodes.get(sourceNodeId);
    node.addEdge(edgeCreator.apply(new Pair<>(targetNodeId, info)));
  }

  public void removeNode(Id<TNode> nodeId) {
    hpaLogger.trace("RemoveNode: %s", nodeId);
    if (nodeId.getIdValue() == 54) {
      System.out.println();
    }
    nodes.remove(nodeId);
  }

  public void removeEdgesFromAndToNode(Id<TNode> nodeId) {
    List<Id<TNode>> keys = new ArrayList<>(nodes.get(nodeId).edges.keySet());
    for (Id<TNode> targetNodeId : keys) {
      nodes.get(targetNodeId).removeEdge(nodeId);
    }
    nodes.get(nodeId).edges.clear();
  }

  public TNode getNode(Id<TNode> nodeId) {
    return nodes.get(nodeId);
  }

  public TNodeInfo getNodeInfo(Id<TNode> nodeId) {
    TNode node = getNode(nodeId);
    assert node != null : "Cannot request node that is null";
    return node.info;
  }

  public Map<Id<TNode>, TEdge> getEdges(Id<TNode> nodeId) {
    return nodes.get(nodeId).edges;
  }
}
