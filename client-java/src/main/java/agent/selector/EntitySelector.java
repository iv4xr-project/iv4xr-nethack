package agent.selector;

import agent.iv4xr.AgentState;
import agent.navigation.strategy.NavUtils;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nethack.enums.EntityType;
import util.CustomVec3D;

public class EntitySelector extends Selector<WorldEntity> {
  public static final EntitySelector money =
      new EntitySelector(SelectionType.SHORTEST, EntityType.GOLD, false);
  public static final EntitySelector adjacentMonster =
      new EntitySelector(SelectionType.ADJACENT, EntityType.MONSTER, false);
  public static final EntitySelector closestMonster =
      new EntitySelector(SelectionType.SHORTEST, EntityType.MONSTER, false);

  public static final EntitySelector food =
      new EntitySelector(SelectionType.SHORTEST, EntityType.EDIBLE, false);

  final EntityType entityType;

  public EntitySelector(
      SelectionType selectionType,
      EntityType entityType,
      Predicate<WorldEntity> predicate,
      boolean adjacent) {
    super(selectionType, predicate, adjacent);
    this.entityType = entityType;
  }

  public EntitySelector(SelectionType selectionType, EntityType entityType, boolean adjacent) {
    super(selectionType, adjacent);
    this.entityType = entityType;
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
    //    if (selectionType == SelectionType.ADJACENT) {
    //      var entity =
    //          entities.stream()
    //              .filter(e -> CustomVec3D.adjacent(new CustomVec3D(e.position), S.loc(), true))
    //              .findFirst();
    //      return entity.orElse(null);
    //    }
  }

  private List<WorldEntity> filter(List<WorldEntity> entities, AgentState S) {
    Stream<WorldEntity> stream = entities.stream();
    if (entityType != null) {
      stream = stream.filter(we -> Objects.equals(we.type, entityType.name()));
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
        "EntitySelector: %s %s (hasPredicate=%b)", selectionType, entityType, predicate != null);
  }
}
