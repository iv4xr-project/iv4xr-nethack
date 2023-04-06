package agent.selector;

import agent.iv4xr.AgentState;
import java.util.List;
import java.util.function.Predicate;

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

  public enum SelectionType {
    FIRST,
    LAST,
    SHORTEST,
    FARTHEST,
    STRAIGHT_ADJACENT,
    ADJACENT
  }
}
