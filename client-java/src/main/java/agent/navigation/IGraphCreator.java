package agent.navigation;

import nl.uu.cs.aplib.utils.Pair;

public interface IGraphCreator<NodeId> {
  void addEdges(Pair<Integer, NodeId> node);

  void removeEdges(Pair<Integer, NodeId> node);

  void addNode(Pair<Integer, NodeId> node);

  void removeNode(Pair<Integer, NodeId> node);
}
