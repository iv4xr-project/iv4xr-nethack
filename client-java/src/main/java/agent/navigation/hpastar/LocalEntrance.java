//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

import eu.iv4xr.framework.spatial.IntVec2D;

public class LocalEntrance {
  public int id;
  // id of the global abstract node
  public int absNodeId;
  // Relative position of entrance inside cluster
  public IntVec2D relativePos;
  public int entranceLocalIdx;
  // local id
  public LocalEntrance(int nodeId, int absNodeId, int localIdx, IntVec2D relativePosition) {
    this.id = nodeId;
    this.absNodeId = absNodeId;
    this.entranceLocalIdx = localIdx;
    this.relativePos = relativePosition;
  }
}
