//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Graph;

import HPASharp.Infrastructure.Id;

public interface IEdge<TNode, TEdgeInfo> {
  Id<TNode> getTargetNodeId() throws Exception;

  void setTargetNodeId(Id<TNode> value) throws Exception;

  TEdgeInfo getInfo() throws Exception;

  void setInfo(TEdgeInfo value) throws Exception;
}
