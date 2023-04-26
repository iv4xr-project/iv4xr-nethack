package agent.strategy;

import static nethack.enums.CommandEnum.*;
import static nl.uu.cs.aplib.AplibEDSL.action;

import agent.iv4xr.AgentState;
import agent.navigation.hpastar.search.Path;
import agent.navigation.hpastar.smoother.Direction;
import agent.navigation.strategy.NavUtils;
import agent.selector.EntitySelector;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import java.util.*;
import nethack.object.Command;
import nethack.object.Entity;
import nethack.object.Player;
import nethack.object.items.Item;
import nethack.world.tiles.Door;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.utils.Pair;
import util.CustomVec2D;
import util.CustomVec3D;
import util.Loggers;
import util.Sounds;

public class Actions {
  // Construct an action that would attack an adjacent monster.
  public static Action attack() {
    return action("attack")
        .do2(
            (AgentState S) ->
                (Direction direction) -> {
                  Sounds.attack();
                  Loggers.GoalLogger.info(">>> attackMonster %s", direction);
                  WorldModel<Player, Entity> newWom = WorldModels.forceAttack(S, direction);
                  return new Pair<>(S, newWom);
                });
  }

  public static Action wield() {
    return action("wield weapon")
        .do2(
            (AgentState S) ->
                (Item item) -> {
                  Loggers.GoalLogger.info(">>> wield weapon %s", item);
                  WorldModel<Player, Entity> newWom =
                      WorldModels.performCommands(
                          S, List.of(new Command(COMMAND_WIELD), new Command(item.symbol)));
                  return new Pair<>(S, newWom);
                });
  }

  // Construct an action that would fire in a direction.
  public static Action fire() {
    return action("fire")
        .do2(
            (AgentState S) ->
                (Direction direction) -> {
                  Sounds.fire();
                  Loggers.GoalLogger.info(">>> fire %s", direction);
                  WorldModel<Player, Entity> newWom = WorldModels.fire(S, direction);
                  return new Pair<>(S, newWom);
                });
  }

  // Construct an action that would open the door.
  public static Action openDoor() {
    return action("open door")
        .do2(
            (AgentState S) ->
                (Direction direction) -> {
                  CustomVec2D doorPos = NavUtils.posInDirection(S.loc().pos, direction);
                  Door d = (Door) S.area().getTile(doorPos);
                  WorldModel<Player, Entity> newWom;
                  if (d.locked) {
                    Sounds.door_kick();
                    Loggers.GoalLogger.info("kick @%s", direction);
                    newWom = WorldModels.kick(S, direction);
                  } else {
                    Sounds.door();
                    Loggers.GoalLogger.info("open door @%s", direction);
                    newWom = WorldModels.open(S, direction);
                  }
                  return new Pair<>(S, newWom);
                });
  }

  public static Action quaffItem() {
    return action("quaff")
        .do2(
            (AgentState S) ->
                (Item item) -> {
                  Loggers.GoalLogger.info(">>> Quaff: %s", item);
                  WorldModel<Player, Entity> newWom = WorldModels.quaffItem(S, item.symbol);
                  return new Pair<>(S, newWom);
                });
  }

  public static Action singleCommand(Command command) {
    return action(String.format("perform command: %s", command))
        .do1(
            (AgentState S) -> {
              Loggers.GoalLogger.info("command: %s", command);
              WorldModel<Player, Entity> newWom = WorldModels.performCommands(S, List.of(command));
              return new Pair<>(S, newWom);
            });
  }

  static Action searchWalls() {
    return action("search")
        .do1(
            (AgentState S) -> {
              Loggers.GoalLogger.info("searchWalls");
              Sounds.search();
              WorldModel<Player, Entity> newWom =
                  WorldModels.performCommands(S, List.of(new Command(COMMAND_SEARCH)));
              return new Pair<>(S, newWom);
            });
  }

  static Action eatItem() {
    return action("eat food")
        .do2(
            (AgentState S) ->
                (Item item) -> {
                  Sounds.eat();
                  Loggers.GoalLogger.info(">>> eatFood: %s", item);
                  WorldModel<Player, Entity> newWom = WorldModels.eatItem(S, item.symbol);
                  return new Pair<>(S, newWom);
                });
  }

  static Action pray() {
    // Only pray once every 900 turns if needed
    return action("pray")
        .do1(
            (AgentState S) -> {
              Loggers.GoalLogger.info(">>> pray");
              WorldModel<Player, Entity> newWom =
                  WorldModels.performCommands(
                      S,
                      List.of(new Command(COMMAND_PRAY), new Command('y'), new Command(MISC_MORE)));
              S.app().gameState.player.lastPrayerTurn = S.app().gameState.stats.turn.time;
              return new Pair<>(S, newWom);
            });
  }

  public static Action interactWorldEntity(EntitySelector entitySelector, List<Command> commands) {
    return action("navAndInteract")
        .do2(
            (AgentState S) ->
                (Path<CustomVec3D> path) -> {
                  if (path.atLocation()) {
                    WorldModel<Player, Entity> newWom = WorldModels.performCommands(S, commands);
                    Entity e = entitySelector.apply(S.app().level().entities, S);
                    newWom.removeElement(e.getId());
                    return new Pair<>(S, newWom);
                  } else {
                    return new Pair<>(S, NavUtils.moveTo(S, path.nextNode()));
                  }
                });
  }
}
