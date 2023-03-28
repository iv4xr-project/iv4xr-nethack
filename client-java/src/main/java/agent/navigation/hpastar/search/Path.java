package agent.navigation.hpastar.search;

import java.util.ArrayList;
import java.util.List;

public class Path<T> {
  public final List<T> nodes;
  public final int cost;

  public Path(List<T> nodes, int cost) {
    assert nodes != null : "Nodes must not be empty, path must be a valid path";
    this.nodes = nodes;
    this.cost = cost;
  }

  public Path() {
    nodes = new ArrayList<>();
    cost = 0;
  }

  public T nextNode() {
    if (atLocation()) {
      return null;
    }

    return nodes.get(1);
  }

  public T destination() {
    if (atLocation()) {
      return null;
    }

    return nodes.get(nodes.size() - 1);
  }

  public boolean atLocation() {
    return nodes.isEmpty();
  }

  public String toString() {
    return String.format("Path (cost=%d): %s", cost, nodes);
  }
}
