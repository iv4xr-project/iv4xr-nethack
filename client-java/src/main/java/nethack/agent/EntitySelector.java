package nethack.agent;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import nethack.object.EntityType;

import java.util.List;
import java.util.stream.Collectors;

public class EntitySelector {
    public static WorldEntity selectFirst(List<WorldEntity> entities) {
        if (entities.size() == 0) {
            return null;
        }
        return entities.get(0);
    }

    public static List<WorldEntity> entityTypeSelector(List<WorldEntity> entities, EntityType entityType) {
        return entities.stream().filter(e -> e.type == entityType.name()).collect(Collectors.toList());
    }

    public static List<WorldEntity> closedDoor(List<WorldEntity> entities) {
        return entities.stream().filter(d -> (boolean) d.properties.get("closed")).collect(Collectors.toList());
    }
}
