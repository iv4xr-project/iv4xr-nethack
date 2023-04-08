package agent.selector;

import agent.iv4xr.AgentState;
import agent.navigation.hpastar.search.Path;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import util.CustomVec3D;

public abstract class Selector<T> {
  protected final SelectionType selectionType;
  protected Predicate<T> predicate = null;
  protected final boolean onlySameLevel = true;
  protected final boolean adjacent;

  public Selector(SelectionType selectionType, Predicate<T> predicate, boolean adjacent) {
    assert (selectionType != SelectionType.ADJACENT
                && selectionType != SelectionType.STRAIGHT_ADJACENT)
            || !adjacent
        : "Does not make sense to be adjacent to an adjacent square";
    this.selectionType = selectionType;
    this.predicate = predicate;
    this.adjacent = adjacent;
  }

  public Selector(SelectionType selectionType, boolean adjacent) {
    assert (selectionType != SelectionType.ADJACENT
                && selectionType != SelectionType.STRAIGHT_ADJACENT)
            || !adjacent
        : "Does not make sense to be adjacent to an adjacent square";
    this.selectionType = selectionType;
    this.adjacent = adjacent;
  }

  public abstract T apply(List<T> elems, AgentState S);

  public abstract T select(List<T> elems, AgentState S);

  public abstract List<T> filter(List<T> elems);

  public Integer selectIndex(List<CustomVec3D> locations, AgentState S) {
    CustomVec3D agentLoc = S.loc();
    if (onlySameLevel) {
      locations =
          locations.stream()
              .filter(location -> location.lvl == agentLoc.lvl)
              .collect(Collectors.toList());
    }

    if (locations.isEmpty()) {
      return null;
    }

    // Adjacent coordinates
    if (selectionType == SelectionType.STRAIGHT_ADJACENT
        || selectionType == SelectionType.ADJACENT) {
      CustomVec3D location = S.loc();
      for (int i = 0; i < locations.size(); i++) {
        CustomVec3D coordinate = locations.get(i);

        // Not on same level
        if (location.lvl != coordinate.lvl) {
          continue;
        }

        if (CustomVec3D.adjacent(location, coordinate, selectionType == SelectionType.ADJACENT)) {
          return i;
        }
      }

      return null;
    }

    // The selection type does not matter if there is no choice
    int n = locations.size();
    if (n == 1 || selectionType == SelectionType.FIRST) {
      return 0;
    }

    if (selectionType == SelectionType.LAST) {
      return n - 1;
    }

    if (selectionType == SelectionType.SHORTEST) {
      return selectShortestIndex(locations, S);
    }

    throw new UnknownError("SelectionType not implemented: " + selectionType);
  }

  public Integer selectShortestIndex(List<CustomVec3D> locations, AgentState S) {
    CustomVec3D agentLoc = S.loc();
    Path<CustomVec3D> shortestPath = S.hierarchicalNav().findShortestPath(agentLoc, locations);
    if (shortestPath == null) {
      return null;
    }

    CustomVec3D target;
    if (shortestPath.atLocation()) {
      target = agentLoc;
    } else {
      target = shortestPath.destination();
    }

    int index = locations.indexOf(target);
    assert index != -1;
    return index;
  }

  public enum SelectionType {
    FIRST,
    LAST,
    SHORTEST,
    FARTHEST,
    STRAIGHT_ADJACENT,
    ADJACENT
  }
}
