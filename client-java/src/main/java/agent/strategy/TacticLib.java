package agent.strategy;

import static nl.uu.cs.aplib.AplibEDSL.*;

import agent.iv4xr.AgentState;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Walkable;
import agent.selector.EntitySelector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import java.util.*;
import java.util.stream.Collectors;
import nethack.object.Player;
import nethack.object.items.FoodItem;
import nethack.object.items.Item;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import util.CustomVec2D;
import util.CustomVec3D;

/**
 * TacticLib Provide several basic actions and tactics.
 *
 * <p>Keep in mind that the provided navigation and exploration tactics/goals currently has no
 * ability to deal with items that block a corridor. The solution is for now to just generate
 * another dungeon where we have no corridors are not blocked by items (use another random seed, for
 * example). A better fix would be to have a smarter navigation and exploration. TO DO.
 *
 * @author wish
 */
public class TacticLib {
  public static Tactic attackAdjacentMonsters() {
    return FIRSTof(
        Actions.attack()
            .on(
                (AgentState S) -> {
                  WorldEntity we =
                      EntitySelector.adjacentMonster.apply(
                          new ArrayList<>(S.worldmodel.elements.values()), S);
                  if (we == null) {
                    return null;
                  }
                  return NavUtils.toDirection(S, new CustomVec3D(we.position));
                })
            .lift(),
        Actions.fire()
            .on(
                (AgentState S) -> {
                  CustomVec3D agentLoc = S.loc();
                  List<WorldEntity> wes =
                      S.worldmodel.elements.values().stream()
                          .filter(
                              worldEntity ->
                                  worldEntity.position != null
                                      && worldEntity.type.equals("MONSTER")
                                      && NavUtils.levelNr(worldEntity.position) == agentLoc.lvl
                                      && CustomVec2D.straightLine(
                                          agentLoc.pos, new CustomVec2D(worldEntity.position)))
                          .collect(Collectors.toList());
                  if (wes.isEmpty()) {
                    return null;
                  }

                  // Get closest entity
                  WorldEntity entity = null;
                  int manhattanDistance = Integer.MAX_VALUE;

                  outer:
                  for (WorldEntity we : wes) {
                    CustomVec2D entityPos = new CustomVec2D(we.position);
                    // Coordinate not visible
                    if (!S.app().gameState.getLevel().visibleCoordinates.contains(entityPos)) {
                      continue;
                    }

                    int xSign = Integer.signum(entityPos.x - agentLoc.pos.x);
                    int ySign = Integer.signum(entityPos.y - agentLoc.pos.y);
                    CustomVec2D delta = new CustomVec2D(xSign, ySign);
                    CustomVec2D currentPos = agentLoc.pos.add(delta);
                    int currentDistance = 1;

                    while (!currentPos.equals(entityPos)) {
                      if (!(S.area().getTile(currentPos) instanceof Walkable)) {
                        continue outer;
                      }
                      currentPos = currentPos.add(delta);
                      currentDistance += 1;
                      if (currentDistance >= manhattanDistance) {
                        continue outer;
                      }
                    }

                    entity = we;
                    manhattanDistance = currentDistance;
                  }

                  if (entity == null) {
                    return null;
                  }

                  CustomVec2D entityPos = new CustomVec2D(entity.position);
                  int xSign = Integer.signum(entityPos.x - agentLoc.pos.x);
                  int ySign = Integer.signum(entityPos.y - agentLoc.pos.y);
                  CustomVec2D delta = new CustomVec2D(xSign, ySign);
                  return NavUtils.toDirection(
                      S, new CustomVec3D(agentLoc.lvl, agentLoc.pos.add(delta)));
                })
            .lift());
  }

  public static Tactic resolveHungerState(int prayerTimeOut) {
    return FIRSTof(
        //      NavTactic.navigateToWorldEntity(new EntitySelector(Selector.SelectionType.CLOSEST,
        // EntityType.EDIBLE)),
        Actions.pray()
            .on(
                (AgentState S) -> {
                  Player player = S.app().gameState.player;
                  if (!player.hungerState.wantsFood()) {
                    return null;
                  }
                  Optional<Integer> lastPrayerTurn = player.lastPrayerTurn;
                  if (lastPrayerTurn.isEmpty()
                      || lastPrayerTurn.get() - S.app().gameState.stats.turn.time > prayerTimeOut) {
                    return true;
                  }
                  return null;
                })
            .lift(),
        Actions.eatItem()
            .on(
                (AgentState S) -> {
                  Player player = S.app().gameState.player;
                  // Player stomach full enough
                  if (!player.hungerState.wantsFood()) {
                    return null;
                  }
                  // Picks the food item with the lowest nutrition per weight
                  List<Item> items =
                      Arrays.stream(player.inventory.items)
                          .filter(item -> item instanceof FoodItem)
                          .sorted(
                              Comparator.comparingDouble(
                                  item -> ((FoodItem) item).food.nutritionPerWeight))
                          .collect(Collectors.toList());
                  if (items.isEmpty()) {
                    return null;
                  }
                  return items.get(0);
                })
            .lift());
  }
}
