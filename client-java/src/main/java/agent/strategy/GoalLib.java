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
                    Actions.singleCommand(new Command(CommandEnum.MISC_WAIT))
                        .on_(Predicates.outOfCombat_HpCritical)
                        .lift(),

                    // Collect money
                    NavTactic.navigateToWorldEntity(EntitySelector.money),

                    // Navigation
                    //                    Actions.kick().on(Predicates.get_lockedDoor()).lift(),
                    Actions.openDoor().on(Predicates.get_closedDoor()).lift(),
                    NavTactic.navigateToTile(TileSelector.adjacentClosedDoorSelector),
                    Actions.searchWalls().on_(Predicates.hidden_tile()).lift(),
                    NavTactic.explore(),
                    //                    NavAction.navigateTo().on((AgentState S) -> {
                    //                      NetHackSurface surface = S.area();
                    //                      List<Tile> tiles = surface.getFrontier();
                    //                      if (tiles.isEmpty()) {
                    //                        return null;
                    //                      }
                    //
                    //                      List<Tile> shortestPath = surface.findShortestPath(new
                    // Tile(NavUtils.loc2(S.worldmodel.position)), tiles);
                    //                      if (shortestPath == null) {
                    //                        return null;
                    //                      }
                    //
                    //                      List<Pair<Integer, Tile>> path =
                    // shortestPath.stream().map(tile -> new
                    // Pair<>(NavUtils.levelNr(S.worldmodel.position),
                    // tile)).collect(Collectors.toList());
                    //                      Loggers.HPALogger.info("Path to %s via %s (%s)",
                    // path.get(1), path.get(path.size() - 1), path);
                    //                      return NavUtils.nextTile(path);
                    //                    }).lift(),

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
