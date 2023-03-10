package agent.strategy;

import static nl.uu.cs.aplib.AplibEDSL.*;

import agent.iv4xr.AgentState;
import agent.navigation.strategy.NavTactic;
import agent.selector.EntitySelector;
import agent.selector.TileSelector;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.enums.CommandEnum;
import nethack.object.Command;
import nl.uu.cs.aplib.mainConcepts.Goal;
import nl.uu.cs.aplib.mainConcepts.GoalStructure;
import nl.uu.cs.aplib.utils.Pair;

public class GoalLib {
  public static GoalStructure explore() {
    Goal G =
        goal("Main")
            .toSolve(
                (Pair<AgentState, WorldModel> proposal) -> {
                  return proposal.fst.app().gameState.done;
                  //                  return tacticLib.explorationExhausted(proposal.fst);
                })
            .withTactic(
                FIRSTof(
                    // Survival
                    TacticLib.attackAdjacentMonsters(),
                    TacticLib.resolveHungerState(900),

                    // Collect money
                    NavTactic.navigateToWorldEntity(EntitySelector.money),

                    // Navigation
                    Actions.kick().on(Predicates.get_lockedDoor()).lift(),
                    Actions.openDoor().on(Predicates.get_closedDoor()).lift(),
                    NavTactic.navigateToTile(TileSelector.adjacentClosedDoorSelector),
                    NavTactic.explore(),

                    // Go to next level
                    NavTactic.navigateToTile(TileSelector.stairDown),
                    Actions.singleCommand(new Command(CommandEnum.MISC_DOWN))
                        .on_(Predicates.on_stairs_down)
                        .lift(),

                    // Explore walls for hidden doors
                    NavTactic.navigateToTile(TileSelector.adjacentWallSelector),
                    Actions.searchWalls().lift(),
                    ABORT()));

    return G.lift(); // REPEAT(G.lift());
  }
}
