package agent.strategy;

import static nl.uu.cs.aplib.AplibEDSL.*;

import agent.iv4xr.AgentState;
import agent.navigation.strategy.NavUtils;
import agent.navigation.surface.Walkable;
import agent.selector.ItemSelector;
import agent.selector.MapSelector;
import agent.selector.Selector;
import java.util.*;
import java.util.stream.Collectors;
import nethack.object.Monster;
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
                  List<CustomVec3D> monsterPositions =
                      NavUtils.addLevelNr(
                          S.app().level().monsters.stream()
                              .filter(monster -> !monster.peaceful)
                              .map(monster -> monster.pos)
                              .collect(Collectors.toList()),
                          S.loc().lvl);
                  Integer index =
                      MapSelector.select(monsterPositions, S, Selector.SelectionType.ADJACENT);
                  if (index == null) {
                    return null;
                  }
                  return NavUtils.toDirection(S, monsterPositions.get(index));
                })
            .lift(),
        Actions.fire()
            .on(
                (AgentState S) -> {
                  Item item =
                      ItemSelector.inventoryQuivered.apply(
                          Arrays.asList(S.app().gameState.player.inventory.items), S);
                  // No quivered items, cannot fire
                  if (item == null) {
                    return null;
                  }

                  CustomVec3D agentLoc = S.loc();
                  List<Monster> monsters =
                      S.app().level().monsters.stream()
                          .filter(
                              monster ->
                                  !monster.peaceful
                                      && CustomVec2D.straightLine(monster.pos, agentLoc.pos))
                          .collect(Collectors.toList());
                  if (monsters.isEmpty()) {
                    return null;
                  }

                  // Get closest entity
                  Monster closestMonster = null;
                  int manhattanDistance = Integer.MAX_VALUE;

                  outer:
                  for (Monster monster : monsters) {
                    // Coordinate not visible
                    if (!S.app().gameState.getLevel().visibleCoordinates.contains(monster.pos)) {
                      continue;
                    }

                    int xSign = Integer.signum(monster.pos.x - agentLoc.pos.x);
                    int ySign = Integer.signum(monster.pos.y - agentLoc.pos.y);
                    CustomVec2D delta = new CustomVec2D(xSign, ySign);
                    CustomVec2D currentPos = agentLoc.pos.add(delta);
                    int currentDistance = 1;

                    while (!currentPos.equals(monster.pos)) {
                      if (!(S.area().getTile(currentPos) instanceof Walkable)) {
                        continue outer;
                      }
                      currentPos = currentPos.add(delta);
                      currentDistance += 1;
                      if (currentDistance >= manhattanDistance) {
                        continue outer;
                      }
                    }

                    closestMonster = monster;
                    manhattanDistance = currentDistance;
                  }

                  // Too far away to reliably hit
                  if (manhattanDistance > 6) {
                    return null;
                  }

                  CustomVec2D monsterPos = closestMonster.pos;
                  int xSign = Integer.signum(monsterPos.x - agentLoc.pos.x);
                  int ySign = Integer.signum(monsterPos.y - agentLoc.pos.y);
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
