//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

import util.CustomVec2D;

public class LocalEntrance {
  public final int id;
  // id of the global abstract node
  public final int absNodeId;
  // Relative position of entrance inside cluster
  public final CustomVec2D relativePos;
  public final int entranceLocalIdx;
  // local id
  public LocalEntrance(int nodeId, int absNodeId, int localIdx, CustomVec2D relativePosition) {
    this.id = nodeId;
    this.absNodeId = absNodeId;
    this.entranceLocalIdx = localIdx;
    this.relativePos = relativePosition;
  }
}
