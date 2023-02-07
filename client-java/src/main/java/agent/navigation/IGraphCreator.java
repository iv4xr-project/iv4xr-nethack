package agent.navigation;

import nl.uu.cs.aplib.utils.Pair;

public interface IGraphCreator<NodeId> {
  public void addEdge(Pair<Integer, NodeId> from, Pair<Integer, NodeId> to);

  public void addNode(Pair<Integer, NodeId> node);

  public void removeNode(Pair<Integer, NodeId> node);

  public void removeEdge(Pair<Integer, NodeId> from, Pair<Integer, NodeId> to);
}
