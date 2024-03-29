package agent.strategy;

import static nl.uu.cs.aplib.AplibEDSL.*;

import agent.iv4xr.AgentState;
import agent.navigation.strategy.NavTactic;
import agent.selector.EntitySelector;
import agent.selector.ItemSelector;
import agent.selector.TileSelector;
import eu.iv4xr.framework.mainConcepts.WorldModel;

import java.util.Arrays;
import java.util.List;
import nethack.enums.CommandEnum;
import nethack.object.Command;
import nethack.object.Entity;
import nethack.object.Player;
import nethack.world.tiles.*;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;

public class GoalLib {
  public static GoalStructure explore() {
    Goal G =
        goal("Main agent explore goal")
            .toSolve(
                (Pair<AgentState, WorldModel<Player, Entity>> proposal) -> proposal.fst.app().gameState.done)
            .withTactic(
                FIRSTof(
                    // Survival
                    TacticLib.attackMonsters(),
                    TacticLib.resolveHungerState(900),
                    Actions.singleCommand(new Command(CommandEnum.MISC_WAIT))
                        .on_(Predicates.outOfCombat_HpCritical)
                        .lift(),

                    // Unlocks navigation to any place
                    Actions.searchWalls().on_(Predicates.hidden_tile()).lift(),
                    Actions.openDoor().on(Predicates.get_closedDoor()).lift(),

                    /*
                    // Collect money and potions
                    action("HIHI")
                        .do1(
                            (AgentState S) -> {
                              System.exit(2);
                              return 0;
                            })
                        .on_(
                            (AgentState S) ->
                                Set.of(Water.class, Pool.class, Moat.class)
                                    .contains(
                                        S.app().level().surface.getTile(S.loc().pos).getClass()))
                        .lift(),
                    NavTactic.navigateToTile(TileSelector.water),
                    */

//                    Actions.quaffItem()
//                        .on(
//                            (AgentState S) ->
//                                ItemSelector.hallucinationPotion.apply(Arrays.asList(S.worldmodel.player.current.inventory.items), S))
//                                                                .lift(),
//                    NavTactic.interactWorldEntity(
//                        EntitySelector.hallucinationPotion, List.of(new Command(CommandEnum.COMMAND_PICKUP))),

//                    NavTactic.interactWorldEntity(
//                        EntitySelector.money, List.of(new Command(CommandEnum.COMMAND_PICKUP))),

                    // Drink a potion when the agent is low hp
//                    Actions.quaffItem()
//                        .on(
//                            (AgentState S) -> {
//                                if (S.app().gameState.player.hp >= 8 || (double) S.app().gameState.player.hpMax / S.app().gameState.player.hp > 0.3) {
//                                  return null;
//                                }
//                                return ItemSelector.healthPotion.apply(Arrays.asList(S.worldmodel.player.current.inventory.items), S); })
//                                                                .lift(),
//                    NavTactic.interactWorldEntity(
//                        EntitySelector.healthPotion, List.of(new Command(CommandEnum.COMMAND_PICKUP))),

                    // Grab ammo for a ranged weapon
//                    NavTactic.interactWorldEntity(EntitySelector.rangedWeaponAmmo, List.of(new Command(CommandEnum.COMMAND_PICKUP))),

                    // Navigation
                    NavTactic.explore(),

                    // Go to next level
                    NavTactic.navigateToTile(TileSelector.stairDown),
                    Actions.singleCommand(new Command(CommandEnum.MISC_DOWN))
                        .on_(Predicates.on_stairs_down)
                        .lift(),
                    ABORT()));

    return G.lift();
  }
}
