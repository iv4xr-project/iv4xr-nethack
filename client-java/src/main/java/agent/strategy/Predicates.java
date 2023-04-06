package agent.strategy;

import agent.iv4xr.AgentState;
import agent.navigation.hpastar.smoother.Direction;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Climbable;
import agent.navigation.surface.Tile;
import agent.selector.EntitySelector;
import agent.selector.TileSelector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import nethack.enums.SymbolType;
import nethack.world.Level;
import nethack.world.tiles.Secret;
import nethack.world.tiles.Stair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import util.CustomVec2D;
import util.CustomVec3D;

public class Predicates {
  public static final Predicate<AgentState> outOfCombat_HpCritical =
      S -> {
        WorldEntity player = S.worldmodel.elements.get(S.worldmodel.agentId);
        if (S.nextToEntity(SymbolType.MONSTER, true)) {
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

  @Contract(pure = true)
  public static @NotNull Predicate<AgentState> hidden_tile() {
    return S -> {
      CustomVec3D agentLoc = S.loc();
      List<CustomVec2D> neighbours = NavUtils.neighbourCoordinates(agentLoc.pos, Level.SIZE, true);
      for (CustomVec2D neighbour : neighbours) {
        CustomVec3D tileLoc = new CustomVec3D(agentLoc.lvl, neighbour);
        Tile tile = S.hierarchicalNav().getTile(tileLoc);
        if (tile instanceof Secret && ((Secret) tile).isSecret()) {
          return true;
        }
      }

      return false;
    };
  }

  public static Predicate<AgentState> on_potion =
      S -> {
        WorldEntity entity =
            EntitySelector.potion.apply(new ArrayList<>(S.worldmodel.elements.values()), S);
        return entity != null && new CustomVec3D(entity.position).equals(S.loc());
      };

  public static Predicate<AgentState> on_stairs_down =
      S -> {
        Tile t = S.area().getTile(NavUtils.loc2(S.worldmodel.position));
        if (!(t instanceof Stair)) {
          return false;
        }
        return ((Stair) t).climbType == Climbable.ClimbType.Down;
      };

  public static List<WorldEntity> findOfType(@NotNull AgentState S, SymbolType type) {
    return S.worldmodel.elements.values().stream()
        .filter(x -> Objects.equals(x.type, type.name()))
        .collect(Collectors.toList());
  }
}
