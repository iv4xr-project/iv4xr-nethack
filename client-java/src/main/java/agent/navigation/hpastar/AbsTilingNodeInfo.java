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
    System.out.println("id: " + id);
    System.out.println("; level: " + level);
    System.out.println("; cluster: " + clusterId);
    System.out.println("; row: " + position.y);
    System.out.println("; col: " + position.x);
    System.out.println("; center: " + centerId);
    System.out.println("; local idx: " + localIdxCluster);
    System.out.println();
  }
}
