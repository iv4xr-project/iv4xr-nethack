//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Graph;

import HPASharp.Infrastructure.Id;

public class AbstractNode implements INode<AbstractNode, AbstractNodeInfo, AbstractEdge> {
  private Id<AbstractNode> __NodeId;

  public Id<AbstractNode> getNodeId() {
    return __NodeId;
  }

  public void setNodeId(Id<AbstractNode> value) {
    __NodeId = value;
  }

  private AbstractNodeInfo __Info;

  public AbstractNodeInfo getInfo() {
    return __Info;
  }

  public void setInfo(AbstractNodeInfo value) {
    __Info = value;
  }

  private IDictionary<Id<AbstractNode>, AbstractEdge> __Edges =
      new IDictionary<Id<AbstractNode>, AbstractEdge>();

  public IDictionary<Id<AbstractNode>, AbstractEdge> getEdges() {
    return __Edges;
  }

  public void setEdges(IDictionary<Id<AbstractNode>, AbstractEdge> value) {
    __Edges = value;
  }

  public AbstractNode(Id<AbstractNode> nodeId, AbstractNodeInfo info) throws Exception {
    setNodeId(nodeId);
    setInfo(info);
    setEdges(new Dictionary<Id<AbstractNode>, AbstractEdge>());
  }

  public void removeEdge(Id<AbstractNode> targetNodeId) throws Exception {
    getEdges().Remove(targetNodeId);
  }

  public void addEdge(AbstractEdge edge) throws Exception {
    if (!getEdges().ContainsKey(edge.getTargetNodeId())
        || getEdges()[edge.getTargetNodeId()].Info.Level < edge.getInfo().getLevel()) {
      getEdges()[edge.getTargetNodeId()] = edge;
    }
  }
}
