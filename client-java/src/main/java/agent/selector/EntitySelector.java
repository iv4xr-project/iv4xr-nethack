package agent.selector;

import agent.iv4xr.AgentState;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nethack.enums.EntityClass;
import nethack.object.Entity;
import util.CustomVec3D;

public class EntitySelector extends Selector<Entity> {
  public static final EntitySelector money =
      new EntitySelector(SelectionType.SHORTEST, EntityClass.COIN, false);
  public static final EntitySelector potion =
      new EntitySelector(SelectionType.SHORTEST, EntityClass.POTION, false);
  public static final EntitySelector food =
      new EntitySelector(SelectionType.SHORTEST, EntityClass.FOOD, false);
  //  public static final EntitySelector freshCorpse =
  //          new EntitySelector(SelectionType.SHORTEST, EntityClass.FOOD, worldEntity ->)

  final EntityClass entityClass;

  public EntitySelector(
      SelectionType selectionType,
      EntityClass entityClass,
      Predicate<Entity> predicate,
      boolean adjacent) {
    super(selectionType, predicate, adjacent);
    this.entityClass = entityClass;
  }

  public EntitySelector(SelectionType selectionType, EntityClass entityClass, boolean adjacent) {
    super(selectionType, adjacent);
    this.entityClass = entityClass;
  }

  @Override
  public Entity apply(List<Entity> entities, AgentState S) {
    List<Entity> filteredEntities = filter(entities);
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

  public List<Entity> filter(List<Entity> entities) {
    Stream<Entity> stream = entities.stream();
    if (entityClass != null) {
      stream = stream.filter(we -> we.entityClass == entityClass);
    }
    if (predicate != null) {
      stream = stream.filter(we -> predicate.test(we));
    }
    return stream.collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return String.format(
        "EntitySelector: %s %s (hasPredicate=%b)", selectionType, entityClass, predicate != null);
  }
}
