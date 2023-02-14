//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

import eu.iv4xr.framework.spatial.IntVec2D;
import util.ColoredStringBuilder;

// implements nodes in the abstract graph
public class AbsTilingNodeInfo {
  public final int id;
  public final IntVec2D position;
  public final int clusterId;
  public final int centerId;
  public final int level;
  public final int localIdxCluster;

  public AbsTilingNodeInfo(
      int id, int level, int clId, IntVec2D position, int centerId, int localIdxCluster) {
    this.id = id;
    this.level = level;
    this.clusterId = clId;
    this.position = position;
    this.centerId = centerId;
    this.localIdxCluster = localIdxCluster;
  }

  public void printInfo() {
    ColoredStringBuilder csb = new ColoredStringBuilder();

    csb.appendf("id: %d%n", id);
    csb.appendf("; level: %d%n", level);
    csb.appendf("; cluster: %d%n", clusterId);
    csb.appendf("; row: %d%n", position.y);
    csb.appendf("; col: %d%n", position.x);
    csb.appendf("; center: %d%n", centerId);
    csb.appendf("; local idx: %d%n", localIdxCluster);

    System.out.println(csb);
  }
}
