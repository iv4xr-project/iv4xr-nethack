package agent;

import agent.navigation.NavUtils;
import agent.navigation.surface.Stair;
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

  public static Function<AgentState, Tile> get_lockedDoor() {
    return S -> {
      Tile t = TileSelector.lockedDoorSelector.apply(S);
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

  public static Predicate<AgentState> near_lockedDoor = S -> get_lockedDoor().apply(S) != null;

  public static Predicate<AgentState> on_stairs_down =
      S -> {
        Tile t = S.area().getTile(NavUtils.loc2(S.worldmodel.position));
        if (!(t instanceof Stair)) {
          return false;
        }
        return !((Stair) t).goesUp;
      };

  public static List<WorldEntity> findOfType(AgentState S, EntityType type) {
    return S.worldmodel.elements.values().stream()
        .filter(x -> Objects.equals(x.type, type.name()))
        .collect(Collectors.toList());
  }
}
