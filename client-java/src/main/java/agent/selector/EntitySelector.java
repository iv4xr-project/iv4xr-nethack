package agent.selector;

import agent.AgentState;
import agent.navigation.NavUtils;
import agent.navigation.NetHackSurface;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.spatial.IntVec2D;
import nethack.object.EntityType;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EntitySelector extends Selector<WorldEntity> {
    public final static EntitySelector closedDoor = new EntitySelector(SelectionType.FIRST, EntityType.DOOR, d -> (boolean)d.properties.get("closed"));
    public final static EntitySelector stairsDown = new EntitySelector(SelectionType.FIRST, EntityType.STAIRS_DOWN);

    EntityType entityType;

    public EntitySelector(SelectionType selectionType, EntityType entityType, Predicate<WorldEntity> predicate) {
        super(selectionType, predicate);
        this.entityType = entityType;
    }

    public EntitySelector(SelectionType selectionType, EntityType entityType) {
        super(selectionType);
        this.entityType = entityType;
    }

    @Override
    public WorldEntity apply(List<WorldEntity> entities, AgentState S) {
        return select(filter(entities), S);
    }

    @Override
    protected WorldEntity select(List<WorldEntity> entities, AgentState S) {
        if (selectionType == SelectionType.FIRST || selectionType == SelectionType.LAST) {
            return super.select(entities, S);
        }

        int n = entities.size();
        // Goes wrong for multiple levels
        IntVec2D agentPos = NavUtils.loc2(S.worldmodel.position);
        float min = NetHackSurface.distSq(agentPos, NavUtils.loc2(entities.get(0).position));
        float max = min;
        int minIndex = 0, maxIndex = 0;
        for (int i = 1; i < n; i++) {
            WorldEntity we = entities.get(i);
            float dist = NetHackSurface.distSq(agentPos, NavUtils.loc2(we.position));
            if (dist < min) {
                min = dist;
                minIndex = i;
            } else if (dist > max) {
                max = dist;
                maxIndex = i;
            }
        }

        if (selectionType == SelectionType.CLOSEST) {
            return entities.get(minIndex);
        } else if (selectionType == SelectionType.FARTHEST) {
            return entities.get(maxIndex);
        } else {
            throw new UnknownError("SelectionType not implemented: " + selectionType);
        }
    }

    private List<WorldEntity> filter(List<WorldEntity> entities) {
        Stream<WorldEntity> stream;
        if (entityType != null && predicate != null) {
            stream = entities.stream().filter(we -> Objects.equals(we.type, entityType.name()) && predicate.test(we));
        } else if (entityType != null) {
            stream = entities.stream().filter(we -> Objects.equals(we.type, entityType.name()));
        } else if (predicate != null) {
            stream = entities.stream().filter(we -> predicate.test(we));
        } else {
            stream = entities.stream();
        }
        return stream.collect(Collectors.toList());
    }
}
