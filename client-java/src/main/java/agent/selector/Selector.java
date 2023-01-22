package agent.selector;

import agent.AgentState;

import java.util.List;
import java.util.function.Predicate;

public abstract class Selector<T> {
    protected SelectionType selectionType;
    protected Predicate<T> predicate = null;
    protected boolean onlySameLevel = true;
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
        } else if (selectionType == SelectionType.FIRST) {
            return elems.get(0);
        } else if (selectionType == SelectionType.LAST) {
            return elems.get(n - 1);
        } else {
            throw new IllegalArgumentException("Cannot call general select for: " + selectionType);
        }
    }

    protected enum SelectionType {FIRST, LAST, CLOSEST, FARTHEST}
}