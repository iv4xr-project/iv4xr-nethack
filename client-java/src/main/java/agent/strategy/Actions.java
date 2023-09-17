package agent.strategy;

import static nl.uu.cs.aplib.AplibEDSL.action;

import agent.iv4xr.AgentState;
import agent.navigation.hpastar.search.Path;
import agent.navigation.hpastar.smoother.Direction;
import agent.navigation.strategy.NavUtils;
import agent.selector.EntitySelector;
import java.util.*;
import nethack.enums.Skill;
import nethack.object.Command;
import nethack.object.Entity;
import nethack.object.items.Item;
import nethack.object.items.WeaponItem;
import nethack.world.tiles.Door;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.utils.Pair;
import util.*;

public class Actions {
  // Construct an action that would attack an adjacent monster.
  public static Action attack() {
    return action("attack")
        .do2(
            (AgentState S) ->
                (Direction direction) -> {
                  Loggers.GoalLogger.info(">>> fight %s", direction);
                  Sounds.attack();
                  S.app().fight(direction);
                  return S.getNewWOM();
                });
  }

  public static Action wield() {
    return action("wield weapon")
        .do2(
            (AgentState S) ->
                (Item item) -> {
                  Loggers.GoalLogger.info(">>> wield weapon %s", item);
                  S.app().wield(item);
                  return S.getNewWOM();
                });
  }

  // Construct an action that would fire in a direction.
  public static Action fire() {
    return action("fire")
        .do2(
            (AgentState S) ->
                (Pair<Item, Direction> info) -> {
                  WeaponItem item = (WeaponItem) info.fst;
                  Direction direction = info.snd;
                  Loggers.GoalLogger.info(">>> fire %s %s", item, direction);
                  Sounds.fire();
                  // A dagger needs to be thrown instead of firing
                  if (item.entityInfo.skill == Skill.DAGGER) {
                    S.app().throwDagger(item, direction);
                  } else {
                    S.app().fire(direction);
                  }
                  return S.getNewWOM();
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

                  if (!d.locked) {
                    Loggers.GoalLogger.info("open door @%s", direction);
                    Sounds.door();
                    S.app().open(direction);
                  } else {
                    Loggers.GoalLogger.info("kick @%s", direction);
                    Sounds.door_kick();
                    S.app().kick(direction);
                  }
                  return S.getNewWOM();
                });
  }

  public static Action quaffItem() {
    return action("quaff")
        .do2(
            (AgentState S) ->
                (Item item) -> {
                  Loggers.GoalLogger.info(">>> Quaff: %s", item);
                  S.app().quaff(item);
                  System.exit(101);
                  return S.getNewWOM();
                });
  }

  public static Action singleCommand(Command command) {
    return action(String.format("perform command: %s", command))
        .do1(
            (AgentState S) -> {
              Loggers.GoalLogger.info("command: %s", command);
              S.app().step(List.of(command));
              return S.getNewWOM();
            });
  }

  static Action searchWalls() {
    return action("search")
        .do1(
            (AgentState S) -> {
              Loggers.GoalLogger.info("searchWalls");
              Sounds.search();
              S.app().search();
              return S.getNewWOM();
            });
  }

  static Action eatItem() {
    return action("eat food")
        .do2(
            (AgentState S) ->
                (Item item) -> {
                  Sounds.eat();
                  Loggers.GoalLogger.info(">>> eatFood: %s", item);
                  S.app().eat(item);
                  return S.getNewWOM();
                });
  }

  static Action pray() {
    // Only pray once every 900 turns if needed
    return action("pray")
        .do1(
            (AgentState S) -> {
              Loggers.GoalLogger.info(">>> pray");
              S.app().pray();
              return S.getNewWOM();
            });
  }

  public static Action interactWorldEntity(EntitySelector entitySelector, List<Command> commands) {
    return action("navAndInteract")
        .do2(
            (AgentState S) ->
                (Path<CustomVec3D> path) -> {
                  if (path.atLocation()) {
                    S.app().step(commands);
                    Entity e = entitySelector.apply(S.app().level().entities, S);
                    S.worldmodel.removeElement(e.getId());
                  } else {
                    NavUtils.moveTo(S, path.nextNode());
                  }
                  return S.getNewWOM();
                });
  }
}
