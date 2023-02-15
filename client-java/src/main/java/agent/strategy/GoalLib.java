package agent.strategy;

import static nl.uu.cs.aplib.AplibEDSL.*;

import agent.iv4xr.AgentState;
import agent.navigation.strategy.NavTactic;
import agent.selector.TileSelector;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;

public class GoalLib {
  public static GoalStructure explore() {
    Goal G =
        goal("Main")
            .toSolve(
                (Pair<AgentState, WorldModel> proposal) -> {
                  return false;
                  //          return tacticLib.explorationExhausted(proposal.fst);
                })
            .withTactic(
                FIRSTof(
                    //                    TacticLib.abortOnDeath(),
                    // Survival
                    Actions.attackMonster().on_(Predicates.inCombat_and_hpNotCritical).lift(),
                    //                    Actions.eatFood().lift(),
                    //                    NavTactic.navigateToWorldEntity(EntitySelector.money),

                    // Navigation
                    Actions.kickDoor().on(Predicates.get_lockedDoor()).lift(),
                    Actions.openDoor().on(Predicates.get_closedDoor()).lift(),
                    NavTactic.navigateNextToTile(TileSelector.closedDoorSelector, false),
                    NavTactic.explore(),

                    //                    // Go to next level
                    //                    NavTactic.navigateToTile(TileSelector.stairDown),
                    //
                    // Actions.singleAction(Command.MISC_DOWN).on_(Predicates.on_stairs_down).lift(),
                    //
                    //                    // Explore walls for hidden doors
                    //                    NavTactic.navigateNextToTile(TileSelector.wallSelector,
                    // true),
                    //                    Actions.searchWalls().lift(),
                    ABORT()));

    return G.lift();
    //    return REPEAT(G.lift());
  }
}
