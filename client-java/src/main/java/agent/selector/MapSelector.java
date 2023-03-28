package agent.selector;

import agent.iv4xr.AgentState;
import agent.navigation.hpastar.search.Path;
import java.util.List;
import util.CustomVec3D;

public abstract class MapSelector {
  public static Integer select(
      List<CustomVec3D> coordinates, AgentState S, Selector.SelectionType selectionType) {
    if (coordinates.isEmpty()) {
      return null;
    }

    // Adjacent coordinates
    if (selectionType == Selector.SelectionType.ADJACENT) {
      CustomVec3D location = S.loc();
      for (int i = 0; i < coordinates.size(); i++) {
        CustomVec3D coordinate = coordinates.get(i);

        // Not on same level
        if (location.lvl != coordinate.lvl) {
          continue;
        }

        if (CustomVec3D.adjacent(location, coordinate, true)) {
          return i;
        }
      }

      return null;
    }

    // The selection type does not matter if there is no choice
    int n = coordinates.size();
    if (n == 1 || selectionType == Selector.SelectionType.FIRST) {
      return 0;
    }

    if (selectionType == Selector.SelectionType.LAST) {
      return n - 1;
    }

    if (selectionType == Selector.SelectionType.SHORTEST) {
      return selectShortest(coordinates, S);
    }

    throw new UnknownError("SelectionType not implemented: " + selectionType);
  }

  public static Integer selectShortest(List<CustomVec3D> locations, AgentState S) {
    CustomVec3D agentLoc = S.loc();
    Path<CustomVec3D> shortestPath = S.hierarchicalNav().findShortestPath(agentLoc, locations);
    if (shortestPath == null || shortestPath.atLocation()) {
      return null;
    }

    CustomVec3D target = shortestPath.destination();
    int index = locations.indexOf(target);
    assert index != -1;
    return index;
  }
}
