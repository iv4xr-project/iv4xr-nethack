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
import util.CustomVec2D;
import util.CustomVec3D;

public class EntitySelector extends Selector<WorldEntity> {
  public static final EntitySelector closedDoor =
      new EntitySelector(
          SelectionType.FIRST, EntityType.DOOR, d -> (boolean) d.properties.get("closed"), false);
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
    if (entities.isEmpty()) {
      return null;
    }

    if (selectionType == SelectionType.ADJACENT) {
      var entity =
          entities.stream()
              .filter(e -> CustomVec3D.adjacent(new CustomVec3D(e.position), S.loc(), true))
              .findFirst();
      return entity.orElse(null);
    }

    int n = entities.size();
    if (n == 1 || selectionType == SelectionType.FIRST) {
      return entities.get(0);
    }

    if (selectionType == SelectionType.LAST) {
      return entities.get(n - 1);
    }

    // Goes wrong for multiple levels
    CustomVec2D agentPos = S.loc().pos;
    float min = CustomVec2D.distSq(agentPos, new CustomVec2D(entities.get(0).position));
    float max = min;
    int minIndex = 0, maxIndex = 0;
    for (int i = 1; i < n; i++) {
      WorldEntity we = entities.get(i);
      float dist = CustomVec2D.distSq(agentPos, NavUtils.loc2(we.position));
      if (dist < min) {
        min = dist;
        minIndex = i;
      } else if (dist > max) {
        max = dist;
        maxIndex = i;
      }
    }

    if (selectionType == SelectionType.SHORTEST) {
      return entities.get(minIndex);
    } else if (selectionType == SelectionType.FARTHEST) {
      return entities.get(maxIndex);
    } else {
      throw new UnknownError("SelectionType not implemented: " + selectionType);
    }
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
