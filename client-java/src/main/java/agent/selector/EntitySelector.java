package agent.selector;

import agent.iv4xr.AgentState;
import agent.navigation.strategy.NavUtils;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nethack.enums.EntityClass;
import util.CustomVec3D;

public class EntitySelector extends Selector<WorldEntity> {
  public static final EntitySelector money =
      new EntitySelector(SelectionType.SHORTEST, EntityClass.COIN, false);
  public static final EntitySelector potion =
      new EntitySelector(SelectionType.SHORTEST, EntityClass.POTION, false);
  public static final EntitySelector food =
      new EntitySelector(SelectionType.SHORTEST, EntityClass.FOOD, false);

  final EntityClass entityClass;

  public EntitySelector(
      SelectionType selectionType,
      EntityClass entityClass,
      Predicate<WorldEntity> predicate,
      boolean adjacent) {
    super(selectionType, predicate, adjacent);
    this.entityClass = entityClass;
  }

  public EntitySelector(SelectionType selectionType, EntityClass entityClass, boolean adjacent) {
    super(selectionType, adjacent);
    this.entityClass = entityClass;
  }

  @Override
  public WorldEntity apply(List<WorldEntity> entities, AgentState S) {
    List<WorldEntity> filteredEntities = filter(entities, S);
    return select(filteredEntities, S);
  }

  @Override
  public WorldEntity select(List<WorldEntity> entities, AgentState S) {
    List<CustomVec3D> coordinates =
        entities.stream()
            .map(entity -> new CustomVec3D(entity.position))
            .collect(Collectors.toList());
    Integer index = MapSelector.select(coordinates, S, selectionType);
    if (index == null) {
      return null;
    }
    return entities.get(index);
  }

  private List<WorldEntity> filter(List<WorldEntity> entities, AgentState S) {
    Stream<WorldEntity> stream = entities.stream();
    if (entityClass != null) {
      stream = stream.filter(we -> Objects.equals(we.type, entityClass.name()));
    }
    if (predicate != null) {
      stream = stream.filter(we -> predicate.test(we));
    }
    if (onlySameLevel) {
      stream =
          stream.filter(
              we -> NavUtils.levelNr(we.position) == NavUtils.levelNr(S.worldmodel.position));
    }
    return stream.collect(Collectors.toList());
  }

  @Override
  public String toString() {
    return String.format(
        "EntitySelector: %s %s (hasPredicate=%b)", selectionType, entityClass, predicate != null);
  }
}
