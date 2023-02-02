package agent.navigation.hpastar.factories;

import agent.navigation.hpastar.graph.AbstractEdge;
import java.util.List;

public class NodeBackup {

  public final int level;

  public final List<AbstractEdge> edges;

  public NodeBackup(int level, List<AbstractEdge> edges) {
    this.level = level;
    this.edges = edges;
  }
}
