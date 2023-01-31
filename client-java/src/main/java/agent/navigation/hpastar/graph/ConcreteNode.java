//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Graph;

import HPASharp.Infrastructure.Id;

public class ConcreteNode implements INode<ConcreteNode, ConcreteNodeInfo, ConcreteEdge> {
  private Id<ConcreteNode> __NodeId;

  public Id<ConcreteNode> getNodeId() {
    return __NodeId;
  }

  public void setNodeId(Id<ConcreteNode> value) {
    __NodeId = value;
  }

  private ConcreteNodeInfo __Info;

  public ConcreteNodeInfo getInfo() {
    return __Info;
  }

  public void setInfo(ConcreteNodeInfo value) {
    __Info = value;
  }

  private IDictionary<Id<ConcreteNode>, ConcreteEdge> __Edges =
      new IDictionary<Id<ConcreteNode>, ConcreteEdge>();

  public IDictionary<Id<ConcreteNode>, ConcreteEdge> getEdges() {
    return __Edges;
  }

  public void setEdges(IDictionary<Id<ConcreteNode>, ConcreteEdge> value) {
    __Edges = value;
  }

  public ConcreteNode(Id<ConcreteNode> nodeId, ConcreteNodeInfo info) throws Exception {
    setNodeId(nodeId);
    setInfo(info);
    setEdges(new Dictionary<Id<ConcreteNode>, ConcreteEdge>());
  }

  public void removeEdge(Id<ConcreteNode> targetNodeId) throws Exception {
    getEdges().Remove(targetNodeId);
  }

  public void addEdge(ConcreteEdge edge) throws Exception {
    getEdges()[edge.getTargetNodeId()] = edge;
  }
}
