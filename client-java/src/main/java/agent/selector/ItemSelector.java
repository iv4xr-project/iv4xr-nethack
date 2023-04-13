package agent.selector;

import agent.iv4xr.AgentState;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nethack.enums.EntityClass;
import nethack.enums.Skill;
import nethack.object.items.Item;

public class ItemSelector extends Selector<Item> {
  public static final ItemSelector inventoryFood = new ItemSelector().ofClass(EntityClass.FOOD);
  public static final ItemSelector inventoryQuivered =
      new ItemSelector()
          .ofClass(EntityClass.WEAPON)
          .predicate((item, S) -> item.description.contains("(in quiver)"));
  public static final ItemSelector meleeWeapon =
      new ItemSelector()
          .ofClass(EntityClass.WEAPON)
          .predicate(
              (item, S) ->
                  item.entityInfo.skill != Skill.BOW
                      && item.entityInfo.skill != Skill.CROSSBOW
                      && item.entityInfo.skill != Skill.DAGGER);
  public static final ItemSelector rangedWeapon =
      new ItemSelector()
          .ofClass(EntityClass.WEAPON)
          .predicate(
              (item, S) ->
                  item.entityInfo.skill == Skill.BOW
                      || item.entityInfo.skill == Skill.CROSSBOW
                      || item.entityInfo.skill == Skill.DAGGER);

  EntityClass entityClass;

  public ItemSelector() {
    // Default selectionType is first for items
    this.selectionType = SelectionType.FIRST;
  }

  public ItemSelector ofClass(EntityClass entityClass) {
    this.entityClass = entityClass;
    return this;
  }

  public ItemSelector selectionType(SelectionType selectionType) {
    super.selectionType(selectionType);
    return this;
  }

  public ItemSelector sameLvl(boolean onlySameLevel) {
    super.sameLvl(onlySameLevel);
    return this;
  }

  public ItemSelector predicate(BiPredicate<Item, AgentState> predicate) {
    super.predicate(predicate);
    return this;
  }

  public ItemSelector globalPredicate(Predicate<AgentState> predicate) {
    super.globalPredicate(predicate);
    return this;
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

  public List<Item> filter(List<Item> items, AgentState S) {
    if (globalPredicate != null && !globalPredicate.test(S)) {
      return new ArrayList<>();
    }

    Stream<Item> stream = items.stream();
    if (entityClass != null) {
      stream =
          stream.filter(i -> i != null && Objects.equals(i.entityInfo.entityClass, entityClass));
    }
    if (predicate != null) {
      stream = stream.filter(i -> i != null && predicate.test(i, S));
    }
    return stream.collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return String.format(
        "ItemSelector: %s %s (hasPredicate=%b)", selectionType, entityClass, predicate != null);
  }
}
