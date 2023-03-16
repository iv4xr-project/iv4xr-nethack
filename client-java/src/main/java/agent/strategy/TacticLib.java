package agent.strategy;

import static nl.uu.cs.aplib.AplibEDSL.*;

import agent.iv4xr.AgentState;
import agent.navigation.strategy.NavUtils;
import agent.selector.EntitySelector;
import agent.selector.ItemSelector;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import nethack.object.Player;
import nl.uu.cs.aplib.mainConcepts.Tactic;

/**
 * Provide several basic actions and tactics.
 *
 * <p>Keep in mind that the provided navigation and exploration tactics/goals currently has no
 * ability to deal with items that block a corridor. The solution is for now to just generate
 * another dungeon where we have no corridors are not blocked by items (use another random seed, for
 * example). A better fix would be to have a smarter navigation and exploration. TO DO.
 *
 * @author wish
 */
public class TacticLib {
  /**
   * This constructs a "default" tactic to interact with an entity. The tactic is enabled if the
   * entity is known in the agent's state/wom, and if it is adjacent to the agent.
   */
  public Tactic interactTac(String targetId) {
    return Actions.interact(targetId)
        .on(
            (AgentState S) -> {
              WorldEntity e = S.worldmodel.elements.get(targetId);
              if (e == null
                  || NavUtils.levelNr(S.worldmodel.position) != NavUtils.levelNr(e.position)) {
                return null;
              }
              if (NavUtils.adjacent(
                  NavUtils.loc2(S.worldmodel.position), NavUtils.loc2(e.position), true)) {
                return NavUtils.loc2(e.position);
              }
              return null;
            })
        .lift();
  }

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
                  return NavUtils.toDirection(S, NavUtils.loc3(we.position));
                })
            //        .lift(),
            //            Actions.fire().on((AgentState S) -> {
            //              int agentLvl = NavUtils.levelNr(S.worldmodel.position);
            //              List<WorldEntity> wes =
            // S.worldmodel.elements.values().stream().filter(worldEntity ->
            // NavUtils.levelNr(worldEntity.position) == agentLvl)).collect(Collectors.toList());
            //              if (wes.isEmpty()) {
            //                return null;
            //              }
            //              WorldEntity entity = null;
            //              IntVec2D agentPos = NavUtils.loc2(S.worldmodel.position);
            //              for (WorldEntity we : wes) {
            //                IntVec2D entityPos = NavUtils.loc2(we.position);
            //                if (agentPos.x == entityPos.x || agentPos.y == entityPos.y) {
            //                  entity = true;
            //                }
            //              }
            //
            //              return NavUtils.toDirection(S, NavUtils.loc3(we.position));})
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
                  return ItemSelector.inventoryFood.apply(Arrays.asList(player.inventory.items), S);
                })
            .lift());
  }
}
