package agent.navigation.path;

import agent.navigation.hpastar.search.Path;
import java.util.List;
import util.CustomVec3D;

public class Path3D extends Path<CustomVec3D> {
  public Path3D(List<CustomVec3D> nodes, int cost) {
    super(nodes, cost);
  }
}
