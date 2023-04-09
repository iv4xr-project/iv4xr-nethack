package agent.selector;

import agent.iv4xr.AgentState;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nethack.object.Monster;
import util.CustomVec3D;

public class MonsterSelector extends Selector<Monster> {
  public static final MonsterSelector aggressive =
      new MonsterSelector().predicate((monster, S) -> !monster.peaceful);
  public static final MonsterSelector adjacentAggressive =
      aggressive.selectionType(SelectionType.ADJACENT);
  String monsterName = null;

  public MonsterSelector() {}

  public MonsterSelector withName(String monsterName) {
    this.monsterName = monsterName;
    return this;
  }

  public MonsterSelector selectionType(SelectionType selectionType) {
    super.selectionType(selectionType);
    return this;
  }

  public MonsterSelector sameLvl(boolean onlySameLevel) {
    super.sameLvl(onlySameLevel);
    return this;
  }

  public MonsterSelector predicate(BiPredicate<Monster, AgentState> predicate) {
    super.predicate(predicate);
    return this;
  }

  public MonsterSelector globalPredicate(Predicate<AgentState> predicate) {
    super.globalPredicate(predicate);
    return this;
  }

  @Override
  public Monster apply(List<Monster> monsters, AgentState S) {
    List<Monster> filteredMonsters = filter(monsters, S);
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

  public List<Monster> filter(List<Monster> monsters, AgentState S) {
    if (globalPredicate != null && !globalPredicate.test(S)) {
      return new ArrayList<>();
    }

    Stream<Monster> stream = monsters.stream();
    if (monsterName != null) {
      stream = stream.filter(monster -> monster.monsterInfo.name.equals(monsterName));
    }
    if (predicate != null) {
      stream = stream.filter(monster -> predicate.test(monster, S));
    }
    return stream.collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return String.format(
        "MonsterSelector: %s %s (hasPredicate=%b)", selectionType, monsterName, predicate != null);
  }
}
