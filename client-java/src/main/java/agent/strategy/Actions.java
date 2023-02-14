package agent.strategy;

import static nl.uu.cs.aplib.AplibEDSL.action;

import agent.iv4xr.AgentState;
import agent.navigation.NetHackSurface;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Door;
import agent.navigation.surface.Tile;
import agent.navigation.surface.Wall;
import agent.selector.ItemSelector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.IntVec2D;
import eu.iv4xr.framework.spatial.Vec3;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import nethack.enums.Command;
import nethack.enums.EntityType;
import nethack.enums.HungerState;
import nethack.object.Item;
import nethack.object.Level;
import nethack.object.Player;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.Logger;
import util.Loggers;
import util.Sounds;

public class Actions {
  static final Logger logger = Loggers.GoalLogger;

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
                  logger.info(">>> interact %s", nextTile);
                  return new Pair<>(S, newwom);
                });
  }

  // Construct an action that would attack an adjacent monster.
  static Action attackMonster() {
    return action("attack")
        .do1(
            (AgentState S) -> {
              Sounds.attack();
              List<WorldEntity> ms = S.adjacentEntities(EntityType.MONSTER, true);
              // just choose the first one:
              Vec3 position = ms.get(0).position;
              logger.info(">>> attackMonster %s", position);
              WorldModel newwom = NavUtils.moveTo(S, position);
              return new Pair<>(S, newwom);
            });
  }

  // Construct an action that would kick the door.
  static Action kickDoor() {
    return action("kick door")
        .do2(
            (AgentState S) ->
                (Pair<Integer, Tile> door) -> {
                  Sounds.door_kick();
                  logger.info("kickDoor @%s", door);
                  WorldModel newwom = WorldModels.kickDoor(S, door);
                  return new Pair<>(S, newwom);
                });
  }

  // Construct an action that would kick the door.
  static Action openDoor() {
    return action("open door")
        .do2(
            (AgentState S) ->
                (Pair<Integer, Tile> doorTile) -> {
                  Sounds.door();
                  logger.info("open door @%s", doorTile);
                  WorldModel newwom = NavUtils.moveTo(S, doorTile);
                  if (Objects.equals(S.app().gameState.message, "This door is locked.")) {
                    Door d = (Door) doorTile.snd;
                    d.isLocked = true;
                  }
                  return new Pair<>(S, newwom);
                });
  }

  static Action singleAction(Command command) {
    return action(String.format("perform command: %s", command))
        .do1(
            (AgentState S) -> {
              logger.info("command: %s", command);
              WorldModel newwom = WorldModels.performCommand(S, command);
              return new Pair<>(S, newwom);
            });
  }

  static Action searchWalls() {
    return action("search")
        .do2(
            (AgentState S) ->
                (List<Wall> walls) -> {
                  logger.info("searchWalls");
                  Sounds.search();
                  WorldModel newwom = WorldModels.performCommand(S, Command.COMMAND_SEARCH);
                  for (Wall wall : walls) {
                    wall.timesSearched++;
                  }
                  return new Pair<>(S, newwom);
                })
        .on(
            (AgentState S) -> {
              NetHackSurface surface =
                  S.hierarchicalNav.areas.get(NavUtils.levelNr(S.worldmodel.position));
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

  static Action eatFood() {
    return action("eat food")
        .do2(
            (AgentState S) ->
                (Item item) -> {
                  Sounds.eat();
                  logger.info(">>> eatFood: %s", item);
                  WorldModel newwom = WorldModels.eatFood(S, item.symbol);
                  return new Pair<>(S, newwom);
                })
        .on(
            (AgentState S) -> {
              Player player = S.app().gameState.player;
              // Player stomach full enough
              if (player.hungerState == HungerState.NORMAL
                  || player.hungerState == HungerState.SATIATED
                  || player.hungerState == HungerState.OVERSATIATED) {
                return null;
              }
              return ItemSelector.inventoryFood.apply(Arrays.asList(player.inventory.items), S);
            });
  }
}
