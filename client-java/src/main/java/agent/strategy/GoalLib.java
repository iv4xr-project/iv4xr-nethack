package agent.strategy;

import static nl.uu.cs.aplib.AplibEDSL.*;

import agent.iv4xr.AgentState;
import agent.navigation.strategy.NavTactic;
import agent.selector.EntitySelector;
import agent.selector.TileSelector;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import java.util.List;
import nethack.enums.CommandEnum;
import nethack.object.Command;
import nethack.object.Entity;
import nethack.object.Player;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;

public class GoalLib {
  public static GoalStructure explore() {
    Goal G =
        goal("Main")
            .toSolve(
                (Pair<AgentState, WorldModel<Player, Entity>> proposal) -> {
                  return proposal.fst.app().gameState.done;
                  //                  return tacticLib.explorationExhausted(proposal.fst);
                })
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

                    // Collect money and potions
                    NavTactic.interactWorldEntity(
                        EntitySelector.money, List.of(new Command(CommandEnum.COMMAND_PICKUP))),
                    //                    NavTactic.pickupWorldEntity(EntitySelector.potion),

                    // Navigation
                    NavTactic.explore(),

                    // Go to next level
                    NavTactic.navigateToTile(TileSelector.stairDown),
                    Actions.singleCommand(new Command(CommandEnum.MISC_DOWN))
                        .on_(Predicates.on_stairs_down)
                        .lift(),
                    ABORT()));

    return G.lift(); // REPEAT(G.lift());
  }
}
