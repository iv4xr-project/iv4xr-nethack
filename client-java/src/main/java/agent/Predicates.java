package agent;

import agent.navigation.NavUtils;
import agent.navigation.surface.Tile;
import agent.selector.TileSelector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import nethack.enums.EntityType;

public class Predicates {
  public static Predicate<AgentState> inCombat_and_hpNotCritical =
      S -> {
        WorldEntity player = S.worldmodel.elements.get(S.worldmodel.agentId);
        int hp = (int) player.properties.get("hp");
        return hp > 5 && S.nextToEntity(EntityType.MONSTER, true);
      };

  public static Function<AgentState, Tile> get_closedDoor() {
    return S -> {
      Tile t = TileSelector.closedDoorSelector.apply(S);
      if (t == null) {
        return null;
      }
      if (!NavUtils.adjacent(t.pos, NavUtils.loc2(S.worldmodel.position), false)) {
        return null;
      }
      return t;
    };
  }

  public static Predicate<AgentState> near_closedDoor = S -> get_closedDoor().apply(S) != null;

  public static Predicate<AgentState> near_lockedDoor =
      S -> {
        Tile t = TileSelector.lockedDoorSelector.apply(S);
        if (t == null) {
          return false;
        }
        ;
        return NavUtils.adjacent(t.pos, NavUtils.loc2(S.worldmodel.position), false);
      };

  public static Predicate<AgentState> on_stairs_down =
      S -> {
        List<WorldEntity> stairs = findOfType(S, EntityType.STAIRS_DOWN);
        stairs =
            stairs.stream()
                .filter(s -> s.position.equals(S.worldmodel.position))
                .collect(Collectors.toList());
        return stairs.size() > 0;
      };

  public static List<WorldEntity> findOfType(AgentState S, EntityType type) {
    return S.worldmodel.elements.values().stream()
        .filter(x -> Objects.equals(x.type, type.name()))
        .collect(Collectors.toList());
  }
}
