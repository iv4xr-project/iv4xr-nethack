package agent.strategy;

import static nethack.enums.CommandEnum.*;
import static nl.uu.cs.aplib.AplibEDSL.action;

import agent.iv4xr.AgentState;
import agent.navigation.hpastar.smoother.Direction;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Tile;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import java.util.*;
import nethack.object.Command;
import nethack.object.items.Item;
import nethack.world.Level;
import nethack.world.Surface;
import nethack.world.tiles.Door;
import nethack.world.tiles.Wall;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.utils.Pair;
import util.CustomVec2D;
import util.Loggers;
import util.Sounds;

public class Actions {
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

  // Construct an action that would fire in a direction.
  static Action fire() {
    return action("fire")
        .do2(
            (AgentState S) ->
                (Direction direction) -> {
                  Sounds.fire();
                  Loggers.GoalLogger.info(">>> fire %s", direction);
                  WorldModel newWom = WorldModels.fire(S, direction);
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
                  if (S.app().gameState.message.contains("This door is locked.")
                      || Objects.equals(S.app().gameState.message, "")) {
                    CustomVec2D doorPos =
                        NavUtils.posInDirection(NavUtils.loc2(S.worldmodel.position), direction);
                    Door d = (Door) S.area().getTile(doorPos);
                    d.locked = true;
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
              Surface surface = S.area();
              List<CustomVec2D> neighbours =
                  NavUtils.neighbourCoordinates(
                      NavUtils.loc2(S.worldmodel.position), Level.SIZE, true);
              List<Wall> walls = new ArrayList<>();
              for (CustomVec2D neighbour : neighbours) {
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
