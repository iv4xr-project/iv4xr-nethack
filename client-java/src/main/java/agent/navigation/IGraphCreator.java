package agent.navigation;

import nl.uu.cs.aplib.utils.Pair;

public interface IGraphCreator<NodeId> {
  public void addEdges(Pair<Integer, NodeId> node);

  public void removeEdges(Pair<Integer, NodeId> node);

  public void addNode(Pair<Integer, NodeId> node);

  public void removeNode(Pair<Integer, NodeId> node);
}
