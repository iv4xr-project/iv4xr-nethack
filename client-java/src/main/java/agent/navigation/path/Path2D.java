package agent.navigation.path;

import agent.navigation.hpastar.search.Path;
import java.util.List;
import util.CustomVec2D;

public class Path2D extends Path<CustomVec2D> {
  public Path2D(List<CustomVec2D> nodes, int cost) {
    super(nodes, cost);
  }
}
