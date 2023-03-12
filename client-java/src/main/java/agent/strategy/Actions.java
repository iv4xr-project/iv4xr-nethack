package agent.strategy;

import static nethack.enums.CommandEnum.*;
import static nl.uu.cs.aplib.AplibEDSL.action;

import agent.iv4xr.AgentState;
import agent.navigation.NetHackSurface;
import agent.navigation.hpastar.smoother.Direction;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Door;
import agent.navigation.surface.Tile;
import agent.navigation.surface.Wall;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.IntVec2D;
import java.util.*;
import nethack.object.Command;
import nethack.object.Item;
import nethack.object.Level;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.utils.Pair;
import util.Loggers;
import util.Sounds;

public class Actions {
  /**
   * Construct an action that would interact with an entity of the given id. The action's guard is
   * left unconstrained (so the action would always be enabled). You can use the "on" method to add
   * a guard.
   */
  static Action interact(String targetId) {
    return action("interact")
        .do2(
            (AgentState S) ->
                (Pair<Integer, Tile> nextTile) -> {
                  WorldModel newwom = NavUtils.moveTo(S, nextTile);
                  Loggers.GoalLogger.info(">>> interact %s", nextTile);
                  return new Pair<>(S, newwom);
                });
  }

  // Construct an action that would attack an adjacent monster.
  static Action attack() {
    return action("attack")
        .do2(
            (AgentState S) ->
                (Direction direction) -> {
                  Sounds.attack();
                  Loggers.GoalLogger.info(">>> attackMonster %s", direction);
                  WorldModel newWom = WorldModels.forceAttack(S, direction);
                  return new Pair<>(S, newWom);
                });
  }

  // Construct an action that would kick the door.
  static Action kick() {
    return action("kick")
        .do2(
            (AgentState S) ->
                (Direction direction) -> {
                  Sounds.door_kick();
                  Loggers.GoalLogger.info("kick @%s", direction);
                  WorldModel newWom = WorldModels.kick(S, direction);
                  return new Pair<>(S, newWom);
                });
  }

  // Construct an action that would kick the door.
  static Action openDoor() {
    return action("open door")
        .do2(
            (AgentState S) ->
                (Direction direction) -> {
                  Sounds.door();
                  Loggers.GoalLogger.info("open door @%s", direction);
                  WorldModel newWom = WorldModels.open(S, direction);
                  if (Objects.equals(S.app().gameState.message, "This door is locked.")) {
                    IntVec2D doorPos =
                        NavUtils.posInDirection(NavUtils.loc2(S.worldmodel.position), direction);
                    Door d = (Door) S.area().getTile(doorPos);
                    d.isLocked = true;
                  }
                  return new Pair<>(S, newWom);
                });
  }

  static Action quaffItem() {
    return action("quaff")
        .do2(
            (AgentState S) ->
                (Item item) -> {
                  Loggers.GoalLogger.info(">>> Quaff: %s", item);
                  WorldModel newWom = WorldModels.quaffItem(S, item.symbol);
                  return new Pair<>(S, newWom);
                });
  }

  static Action singleCommand(Command command) {
    return action(String.format("perform command: %s", command))
        .do1(
            (AgentState S) -> {
              Loggers.GoalLogger.info("command: %s", command);
              WorldModel newwom = WorldModels.performCommands(S, command);
              return new Pair<>(S, newwom);
            });
  }

  static Action searchWalls() {
    return action("search")
        .do2(
            (AgentState S) ->
                (List<Wall> walls) -> {
                  Loggers.GoalLogger.info("searchWalls");
                  Sounds.search();
                  WorldModel newwom = WorldModels.performCommands(S, new Command(COMMAND_SEARCH));
                  for (Wall wall : walls) {
                    wall.timesSearched++;
                  }
                  return new Pair<>(S, newwom);
                })
        .on(
            (AgentState S) -> {
              NetHackSurface surface = S.area();
              List<IntVec2D> neighbours =
                  NavUtils.neighbourCoordinates(
                      NavUtils.loc2(S.worldmodel.position), Level.SIZE, true);
              List<Wall> walls = new ArrayList<>();
              for (IntVec2D neighbour : neighbours) {
                Tile t = surface.getTile(neighbour);
                if (t instanceof Wall) {
                  walls.add((Wall) t);
                }
              }
              if (walls.isEmpty()) {
                return null;
              }
              return walls;
            });
  }

  static Action eatItem() {
    return action("eat food")
        .do2(
            (AgentState S) ->
                (Item item) -> {
                  Sounds.eat();
                  Loggers.GoalLogger.info(">>> eatFood: %s", item);
                  WorldModel newWom = WorldModels.eatItem(S, item.symbol);
                  return new Pair<>(S, newWom);
                });
  }

  static Action pray() {
    // Only pray once every 900 turns if needed
    return action("pray")
        .do1(
            (AgentState S) -> {
              Loggers.GoalLogger.info(">>> pray");
              WorldModel newWom =
                  WorldModels.performCommands(
                      S, new Command(COMMAND_PRAY), new Command("y"), new Command(MISC_MORE));
              S.app().gameState.player.lastPrayerTurn =
                  Optional.of(S.app().gameState.stats.turn.time);
              return new Pair<>(S, newWom);
            });
  }
}
