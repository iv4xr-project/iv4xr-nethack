package agent.selector;

import agent.iv4xr.AgentState;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nethack.object.Monster;
import util.CustomVec3D;

public class MonsterSelector extends Selector<Monster> {
  public static final MonsterSelector adjacentAggressive =
      new MonsterSelector(SelectionType.ADJACENT, null, monster -> !monster.peaceful, false);

  final String monsterName;

  public MonsterSelector(
      SelectionType selectionType,
      String monsterName,
      Predicate<Monster> predicate,
      boolean adjacent) {
    super(selectionType, predicate, adjacent);
    this.monsterName = monsterName;
  }

  public MonsterSelector(SelectionType selectionType, String monsterName, boolean adjacent) {
    super(selectionType, adjacent);
    this.monsterName = monsterName;
  }

  @Override
  public Monster apply(List<Monster> monsters, AgentState S) {
    List<Monster> filteredMonsters = filter(monsters);
    return select(filteredMonsters, S);
  }

  @Override
  public Monster select(List<Monster> monsters, AgentState S) {
    List<CustomVec3D> coordinates =
        monsters.stream().map(monster -> monster.loc).collect(Collectors.toList());
    Integer index = selectIndex(coordinates, S);
    if (index == null) {
      return null;
    }
    return monsters.get(index);
  }

  public List<Monster> filter(List<Monster> monster) {
    Stream<Monster> stream = monster.stream();
    if (monsterName != null) {
      stream = stream.filter(we -> we.monsterInfo.name.equals(monsterName));
    }
    if (predicate != null) {
      stream = stream.filter(we -> predicate.test(we));
    }
    return stream.collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return String.format(
        "MonsterSelector: %s %s (hasPredicate=%b)", selectionType, monsterName, predicate != null);
  }
}
