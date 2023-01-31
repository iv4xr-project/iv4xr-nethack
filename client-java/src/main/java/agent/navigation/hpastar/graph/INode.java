//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp.Graph;

import HPASharp.Infrastructure.Id;

public interface INode<TId, TInfo, TEdge> {
  Id<TId> getNodeId() throws Exception;

  void setNodeId(Id<TId> value) throws Exception;

  TInfo getInfo() throws Exception;

  void setInfo(TInfo value) throws Exception;

  IDictionary<Id<TId>, TEdge> getEdges() throws Exception;

  void setEdges(IDictionary<Id<TId>, TEdge> value) throws Exception;

  void removeEdge(Id<TId> targetNodeId) throws Exception;

  void addEdge(TEdge targetNodeId) throws Exception;
}
