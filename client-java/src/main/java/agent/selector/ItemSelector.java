package agent.selector;

import agent.iv4xr.AgentState;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nethack.enums.EntityClass;
import nethack.object.items.Item;

public class ItemSelector extends Selector<Item> {
  public static final ItemSelector inventoryFood =
      new ItemSelector(SelectionType.FIRST, EntityClass.FOOD);
  public static final ItemSelector inventoryQuivered =
      new ItemSelector(
          SelectionType.FIRST,
          EntityClass.WEAPON,
          item -> item.description.contains("(in quiver)"));

  final EntityClass entityClass;

  public ItemSelector(
      SelectionType selectionType, EntityClass entityClass, Predicate<Item> predicate) {
    super(selectionType, predicate, false);
    this.entityClass = entityClass;
  }

  public ItemSelector(SelectionType selectionType, EntityClass entityClass) {
    super(selectionType, false);
    this.entityClass = entityClass;
  }

  @Override
  public Item apply(List<Item> items, AgentState S) {
    return select(filter(items, S), S);
  }

  @Override
  public Item select(List<Item> items, AgentState S) {
    if (items.isEmpty()) {
      return null;
    }

    int n = items.size();
    if (n == 1 || selectionType == SelectionType.FIRST) {
      return items.get(0);
    }

    if (selectionType == SelectionType.LAST) {
      return items.get(n - 1);
    }

    throw new IllegalArgumentException("Not valid");
  }

  private List<Item> filter(List<Item> items, AgentState S) {
    Stream<Item> stream = items.stream();
    if (entityClass != null) {
      stream = stream.filter(i -> i != null && Objects.equals(i.type, entityClass));
    }
    if (predicate != null) {
      stream = stream.filter(i -> i != null && predicate.test(i));
    }
    return stream.collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return String.format(
        "ItemSelector: %s %s (hasPredicate=%b)", selectionType, entityClass, predicate != null);
  }
}
