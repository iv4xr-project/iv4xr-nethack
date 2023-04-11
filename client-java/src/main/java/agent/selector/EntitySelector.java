package agent.selector;

import agent.iv4xr.AgentState;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nethack.enums.EntityClass;
import nethack.object.Entity;
import util.CustomVec3D;

public class EntitySelector extends Selector<Entity> {
  public static final EntitySelector money = new EntitySelector().ofClass(EntityClass.COIN);
  public static final EntitySelector potion = new EntitySelector().ofClass(EntityClass.POTION);
  public static final EntitySelector food = new EntitySelector().ofClass(EntityClass.FOOD);
  public static final EntitySelector freshCorpse =
      new EntitySelector()
          .ofClass(EntityClass.FOOD)
          .predicate(
              (entity, S) ->
                  entity.entityInfo.name.equals("corpse")
                      && (S.app().gameState.stats.turn.turnNr - entity.createTurn.turnNr < 50));

  EntityClass entityClass;

  public EntitySelector() {}

  public EntitySelector ofClass(EntityClass entityClass) {
    this.entityClass = entityClass;
    return this;
  }

  public EntitySelector selectionType(SelectionType selectionType) {
    super.selectionType(selectionType);
    return this;
  }

  public EntitySelector sameLvl(boolean onlySameLevel) {
    super.sameLvl(onlySameLevel);
    return this;
  }

  public EntitySelector predicate(BiPredicate<Entity, AgentState> predicate) {
    super.predicate(predicate);
    return this;
  }

  public EntitySelector globalPredicate(Predicate<AgentState> predicate) {
    super.globalPredicate(predicate);
    return this;
  }

  @Override
  public Entity apply(List<Entity> entities, AgentState S) {
    List<Entity> filteredEntities = filter(entities, S);
    return select(filteredEntities, S);
  }

  @Override
  public Entity select(List<Entity> entities, AgentState S) {
    List<CustomVec3D> coordinates =
        entities.stream().map(entity -> entity.loc).collect(Collectors.toList());
    Integer index = selectIndex(coordinates, S);
    if (index == null) {
      return null;
    }
    return entities.get(index);
  }

  public List<Entity> filter(List<Entity> entities, AgentState S) {
    if (globalPredicate != null && !globalPredicate.test(S)) {
      return new ArrayList<>();
    }

    Stream<Entity> stream = entities.stream();
    if (entityClass != null) {
      stream = stream.filter(we -> we.entityInfo.entityClass == entityClass);
    }
    if (predicate != null) {
      stream = stream.filter(we -> predicate.test(we, S));
    }
    return stream.collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return String.format(
        "EntitySelector: %s %s (hasPredicate=%b)", selectionType, entityClass, predicate != null);
  }
}
