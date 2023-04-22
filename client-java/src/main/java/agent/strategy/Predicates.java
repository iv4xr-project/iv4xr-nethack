package agent.strategy;

import agent.iv4xr.AgentState;
import agent.navigation.hpastar.smoother.Direction;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Climbable;
import agent.navigation.surface.Tile;
import agent.selector.EntitySelector;
import agent.selector.MonsterSelector;
import agent.selector.TileSelector;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import nethack.object.Entity;
import nethack.object.Player;
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
        Player player = S.worldmodel.player.current;
        if (!MonsterSelector.adjacentAggressive.filter(S.app().level().monsters, S).isEmpty()) {
          return false;
        }
        float hpPercentage = (float) player.hp / (float) player.hpMax;
        return player.hp < 5 && player.hpMax >= 5 || hpPercentage < 0.40;
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
        Entity entity = EntitySelector.potion.apply(S.app().level().entities, S);
        return entity != null && entity.loc.equals(S.loc());
      };

  public static Predicate<AgentState> on_stairs_down =
      S -> {
        Tile t = S.area().getTile(S.loc().pos);
        if (!(t instanceof Stair)) {
          return false;
        }
        return ((Stair) t).climbType == Climbable.ClimbType.Down;
      };
}
