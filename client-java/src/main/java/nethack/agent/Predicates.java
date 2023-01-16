package nethack.agent;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import nethack.object.EntityType;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Predicates {

    public static Predicate<AgentState> inCombat_and_hpNotCritical = S -> {
        WorldEntity player = S.worldmodel.elements.get(S.worldmodel.agentId);
        int hp = (int) player.properties.get("hp");
        return hp > 5 && S.nextToEntity(EntityType.MONSTER, true);
    };

    public static Predicate<AgentState> near_closedDoor = S -> {
        List<WorldEntity> doors = S.adjacentEntities(EntityType.DOOR, false);
        doors = doors.stream().filter(d -> (boolean) d.properties.get("closed")).collect(Collectors.toList());
        return doors.size() > 0;
    };

    public static WorldEntity closed_door = null;
    public static Predicate<AgentState> closed_door_set = S -> {
        return closed_door != null;
    };

    public static Predicate<AgentState> closed_door_exists = S -> {
        List<WorldEntity> doors = findOfType(S, EntityType.DOOR);
        doors = doors.stream().filter(d -> (boolean) d.properties.get("closed")).collect(Collectors.toList());
        return doors.size() > 0;
    };

    public static List<WorldEntity> findOfType(AgentState S, EntityType type) {
        return S.worldmodel.elements.values().stream().filter(x -> Objects.equals(x.type, type.name())).collect(Collectors.toList());
    }
}