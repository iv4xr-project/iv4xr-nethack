package agent.strategy;

import agent.iv4xr.AgentState;
import agent.navigation.hpastar.smoother.Direction;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Climbable;
import agent.navigation.surface.Tile;
import agent.selector.TileSelector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import nethack.enums.EntityType;
import nethack.world.tiles.Stair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Predicates {
  public static final Predicate<AgentState> outOfCombat_HpCritical =
      S -> {
        WorldEntity player = S.worldmodel.elements.get(S.worldmodel.agentId);
        if (S.nextToEntity(EntityType.MONSTER, true)) {
          return false;
        }
        int hp = (int) player.properties.get("hp");
        int hpMax = (int) player.properties.get("hpmax");
        float hpPercentage = (float) hp / (float) hpMax;
        return hp < 5 || hpPercentage < 0.40;
      };

  @Contract(pure = true)
  public static @NotNull Function<AgentState, Direction> get_closedDoor() {
    return S -> {
      Tile tile = TileSelector.adjacentClosedDoorSelector.apply(S);
      if (tile == null) {
        return null;
      }
      return NavUtils.toDirection(S, tile.loc);
    };
  }
  //
  //  @Contract(pure = true)
  //  public static @NotNull Function<AgentState, Direction> get_lockedDoor() {
  //    return S -> {
  //      Tile tile = TileSelector.lockedDoorSelector.apply(S);
  //      if (tile == null || !CustomVec3D.adjacent(S.loc(), tile.loc, true)) {
  //        return null;
  //      }
  //      return NavUtils.toDirection(S, tile.loc);
  //    };
  //  }

  public static Predicate<AgentState> on_stairs_down =
      S -> {
        Tile t = S.area().getTile(NavUtils.loc2(S.worldmodel.position));
        if (!(t instanceof Stair)) {
          return false;
        }
        return ((Stair) t).climbType == Climbable.ClimbType.Down;
      };

  public static List<WorldEntity> findOfType(@NotNull AgentState S, EntityType type) {
    return S.worldmodel.elements.values().stream()
        .filter(x -> Objects.equals(x.type, type.name()))
        .collect(Collectors.toList());
  }
}
