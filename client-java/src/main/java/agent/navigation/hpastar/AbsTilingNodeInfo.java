//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package agent.navigation.hpastar;

import eu.iv4xr.framework.spatial.IntVec2D;

// implements nodes in the abstract graph
public class AbsTilingNodeInfo {
  public int id;
  public IntVec2D position;
  public int clusterId;
  public int centerId;
  public int level;
  public int localIdxCluster;

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
    StringBuilder sb = new StringBuilder();

    sb.append("id: ").append(id).append(System.lineSeparator());
    sb.append("; level: ").append(level).append(System.lineSeparator());
    sb.append("; cluster: ").append(clusterId).append(System.lineSeparator());
    sb.append("; row: ").append(position.y).append(System.lineSeparator());
    sb.append("; col: ").append(position.x).append(System.lineSeparator());
    sb.append("; center: ").append(centerId).append(System.lineSeparator());
    sb.append("; local idx: ").append(localIdxCluster).append(System.lineSeparator());

    System.out.println(sb.toString());
  }
}
