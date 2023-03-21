package agent.strategy;

import static nl.uu.cs.aplib.AplibEDSL.*;

import agent.iv4xr.AgentState;
import agent.navigation.strategy.NavUtils;
import agent.selector.EntitySelector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import java.util.*;
import java.util.stream.Collectors;
import nethack.object.Player;
import nethack.object.items.FoodItem;
import nethack.object.items.Item;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import util.CustomVec3D;

/**
 * CustomVec2D Provide several basic actions and tactics.
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
                }) // .lift(),
            //        Actions.fire().on((AgentState S) -> {
            //               int agentLvl = NavUtils.levelNr(S.worldmodel.position);
            //               List<WorldEntity> wes =
            //                 S.worldmodel.elements.values().stream().filter(worldEntity ->
            //                         NavUtils.levelNr(worldEntity.position) ==
            //                 agentLvl)).collect(Collectors.toList());
            //                 if (wes.isEmpty()) {
            //                            return null;
            //                          }
            //                          WorldEntity entity = null;
            //                          CustomVec2D agentPos = NavUtils.loc2(S.worldmodel.position);
            //                          for (WorldEntity we : wes) {
            //                  CustomVec2D      CustomVec2D entityPos = NavUtils.loc2(we.position);
            //                            if (agentPos.x == entityPos.x || agentPos.y ==
            // entityPos.y) {
            //                CustomVec2D      entity = true;
            //                            }
            //                          }
            //
            //                          return NavUtils.toDirection(S,
            // NavUtils.loc3(we.position));})
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
