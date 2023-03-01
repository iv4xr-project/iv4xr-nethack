package agent.selector;

import agent.iv4xr.AgentState;
import java.util.List;
import java.util.function.Predicate;

public abstract class Selector<T> {
  protected final SelectionType selectionType;
  protected Predicate<T> predicate = null;
  protected final boolean onlySameLevel = true;

  public Selector(SelectionType selectionType, Predicate<T> predicate) {
    this.selectionType = selectionType;
    this.predicate = predicate;
  }

  public Selector(SelectionType selectionType) {
    this.selectionType = selectionType;
  }

  public abstract T apply(List<T> elems, AgentState S);

  protected T select(List<T> elems, AgentState S) {
    int n = elems.size();
    if (n == 0) {
      return null;
    }
    assert selectionType == SelectionType.FIRST || selectionType == SelectionType.LAST
        : "No general selection implemented for " + selectionType;
    if (selectionType == SelectionType.FIRST) {
      return elems.get(0);
    } else {
      return elems.get(n - 1);
    }
  }

  protected enum SelectionType {
    FIRST,
    LAST,
    CLOSEST,
    FARTHEST,
    ADJACENT
  }
}
