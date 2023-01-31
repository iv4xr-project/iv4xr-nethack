//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Graph;

import HPASharp.Infrastructure.Id;

public class AbstractEdge implements IEdge<AbstractNode, AbstractEdgeInfo> {
  private Id<AbstractNode> __TargetNodeId;

  public Id<AbstractNode> getTargetNodeId() {
    return __TargetNodeId;
  }

  public void setTargetNodeId(Id<AbstractNode> value) {
    __TargetNodeId = value;
  }

  private AbstractEdgeInfo __Info;

  public AbstractEdgeInfo getInfo() {
    return __Info;
  }

  public void setInfo(AbstractEdgeInfo value) {
    __Info = value;
  }

  public AbstractEdge(Id<AbstractNode> targetNodeId, AbstractEdgeInfo info) throws Exception {
    setTargetNodeId(targetNodeId);
    setInfo(info);
  }
}
