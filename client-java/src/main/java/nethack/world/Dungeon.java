package nethack.world;

import agent.navigation.HierarchicalNavigation;
import agent.navigation.hpastar.graph.AbstractNode;
import agent.navigation.hpastar.infrastructure.Constants;
import agent.navigation.hpastar.infrastructure.Id;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nethack.object.Dlvl;
import nethack.object.Player;

public class Dungeon {
  public HierarchicalNavigation hierarchicalNav = new HierarchicalNavigation(new Surface());
  public final List<Level> levels = new ArrayList<>();
  public final Map<Dlvl, Integer> indexes = new HashMap<>();

  public Level getLevel(Dlvl dlvl) {
    return levels.get(getLevelNr(dlvl));
  }

  // Player info to know the edge
  public void newLevel(Level level, Dlvl dlvl, Player player) {
    assert !levelExists(dlvl) : String.format("Level %s was found but newLevel is called", dlvl);
    indexes.put(dlvl, levels.size());
    levels.add(level);

    // Player has no previous location
    if (player == null || player.previousLocation == null) {
      hierarchicalNav = new HierarchicalNavigation(level.surface);
      return;
    }

    hierarchicalNav.addNextArea(level.surface);

    Id<AbstractNode> previousNodeId =
        hierarchicalNav.hierarchicalGraph.addAbstractNode(player.previousLocation);
    Id<AbstractNode> currentNodeId =
        hierarchicalNav.hierarchicalGraph.addAbstractNode(player.location);

    AbstractNode previousNode =
        hierarchicalNav.hierarchicalGraph.abstractGraph.getNode(previousNodeId);
    AbstractNode currentNode =
        hierarchicalNav.hierarchicalGraph.abstractGraph.getNode(currentNodeId);
    if (!previousNode.edges.containsKey(currentNodeId)) {
      hierarchicalNav.hierarchicalGraph.addEdge(previousNodeId, currentNodeId, Constants.COST_ONE);
    }
    if (!currentNode.edges.containsKey(previousNodeId)) {
      hierarchicalNav.hierarchicalGraph.addEdge(currentNodeId, previousNodeId, Constants.COST_ONE);
    }
  }

  public boolean levelExists(Dlvl dlvl) {
    return indexes.containsKey(dlvl);
  }

  public int getLevelNr(Dlvl dlvl) {
    assert levelExists(dlvl) : String.format("Level %s not found", dlvl);
    return indexes.get(dlvl);
  }
}
