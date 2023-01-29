package agent.strategy;

import agent.iv4xr.AgentState;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Climbable;
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
import nl.uu.cs.aplib.utils.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Predicates {
  public static Predicate<AgentState> inCombat_and_hpNotCritical =
      S -> {
        WorldEntity player = S.worldmodel.elements.get(S.worldmodel.agentId);
        int hp = (int) player.properties.get("hp");
        return hp > 5 && S.nextToEntity(EntityType.MONSTER, true);
      };

  @Contract(pure = true)
  public static @NotNull Function<AgentState, Pair<Integer, Tile>> get_closedDoor() {
    return S -> {
      Pair<Integer, Tile> t = TileSelector.closedDoorSelector.apply(S);
      if (t == null) {
        return null;
      }
      if (!NavUtils.adjacent(t.snd.pos, NavUtils.loc2(S.worldmodel.position), false)) {
        return null;
      }
      return t;
    };
  }

  @Contract(pure = true)
  public static @NotNull Function<AgentState, Pair<Integer, Tile>> get_lockedDoor() {
    return S -> {
      Pair<Integer, Tile> t = TileSelector.lockedDoorSelector.apply(S);
      if (t == null) {
        return null;
      }
      if (!NavUtils.adjacent(t.snd.pos, NavUtils.loc2(S.worldmodel.position), false)) {
        return null;
      }
      return t;
    };
  }

  public static Predicate<AgentState> on_stairs_down =
      S -> {
        Tile t = S.area().getTile(NavUtils.loc2(S.worldmodel.position));
        if (!(t instanceof Stair)) {
          return false;
        }
        return ((Stair) t).climbType == Climbable.ClimbType.Descendable;
      };

  public static List<WorldEntity> findOfType(@NotNull AgentState S, EntityType type) {
    return S.worldmodel.elements.values().stream()
        .filter(x -> Objects.equals(x.type, type.name()))
        .collect(Collectors.toList());
  }
}
