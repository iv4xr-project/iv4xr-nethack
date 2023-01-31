//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Graph;

import HPASharp.Infrastructure.Id;

public class ConcreteEdge implements IEdge<ConcreteNode, ConcreteEdgeInfo> {
  private Id<ConcreteNode> __TargetNodeId;

  public Id<ConcreteNode> getTargetNodeId() {
    return __TargetNodeId;
  }

  public void setTargetNodeId(Id<ConcreteNode> value) {
    __TargetNodeId = value;
  }

  private ConcreteEdgeInfo __Info;

  public ConcreteEdgeInfo getInfo() {
    return __Info;
  }

  public void setInfo(ConcreteEdgeInfo value) {
    __Info = value;
  }

  public ConcreteEdge(Id<ConcreteNode> targetNodeId, ConcreteEdgeInfo info) throws Exception {
    setTargetNodeId(targetNodeId);
    setInfo(info);
  }
}
