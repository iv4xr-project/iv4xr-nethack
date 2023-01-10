package nethack.agent;

import nl.uu.cs.aplib.exampleUsages.miniDungeon.Entity.EntityType;
import nl.uu.cs.aplib.mainConcepts.*;
import nl.uu.cs.aplib.mainConcepts.GoalStructure.PrimitiveGoal;
import static nl.uu.cs.aplib.AplibEDSL.*;
import eu.iv4xr.framework.goalsAndTactics.IInteractiveWorldGoalLib;
import eu.iv4xr.framework.mainConcepts.*;
import nl.uu.cs.aplib.utils.Pair;
import nethack.agent.Utils.*;
import nethack.utils.NethackSurface_NavGraph.Tile;
import nethack.agent.Utils;

import java.util.function.Predicate;

/**
 * Provide several basic goal-structures.
 * 
 * <p>
 * Keep in mind that the provided navigation and exploration tactics/goals
 * currently has no ability to deal with items that block a corridor. The
 * solution is for now to just generate another dungeon where we have no
 * corridors are not blocked by items (use another random seed, for example). A
 * better fix would be to have a smarter navigation and exploration. TO DO.
 * 
 * @author wish
 *
 */
public class GoalLib implements IInteractiveWorldGoalLib<Pair<Integer, Tile>> {

	public TacticLib tacticLib = new TacticLib();

	/**
	 * This will search the maze to guide the agent to a tile next to the specified
	 * entity ("touching" the entity).
	 * 
	 * <p>
	 * The goal's tactic can also handle some critical situations that may emerge
	 * during the search, e.g. if it is attacked by a monster, or when it gets low
	 * in the health.
	 */
	@Override
	public GoalStructure entityInCloseRange(String targetId) {

		var G = goal("Entity " + targetId + " is touched.").toSolve((Pair<AgentState, WorldModel> proposal) -> {
			var S = proposal.fst;
			WorldModel previouswom = S.worldmodel;
			WorldModel newObs = proposal.snd;
			WorldEntity e = previouswom.getElement(targetId);
			if (e == null) {
				return false;
			}
			WorldEntity a = S.worldmodel.elements.get(S.worldmodel().agentId);
			var solved = Utils.levelId(a) == Utils.levelId(e)
					&& Utils.adjacent(Utils.toTile(newObs.position), Utils.toTile(e.position));
			// System.out.println(">>> checking goal") ;
			return solved;
		}).withTactic(FIRSTof(tacticLib.attackMonsterAction().on_(tacticLib.inCombat_and_hpNotCritical).lift(),
				tacticLib.navigateToTac(targetId), tacticLib.explore(null),
				// Abort().on_(S -> { System.out.println("### about to abort") ; return
				// false;}).lift(),
				ABORT()));

		return G.lift();
	}

	GoalStructure checkIfEntityIsInCloseRange(String targetId) {

		return lift("Check if entity " + targetId + " is touched", (AgentState S) -> {
			WorldEntity e = S.worldmodel.getElement(targetId);
			if (e == null) {
				return false;
			}
			WorldEntity a = S.worldmodel.elements.get(S.worldmodel().agentId);
			return Utils.levelId(a) == Utils.levelId(e)
					&& Utils.adjacent(Utils.toTile(S.worldmodel.position), Utils.toTile(e.position));
		});
	}

	@Override
	public GoalStructure exploring(Pair<Integer, Tile> heuristicLocation, int budget) {
		GoalStructure explr = goal("exploring (persistent-goal: aborted when it is terminated)")
				.toSolve(belief -> false)
				.withTactic(FIRSTof(tacticLib.attackMonsterAction().on_(tacticLib.inCombat_and_hpNotCritical).lift(),
						tacticLib.explore(heuristicLocation), ABORT()))
				.lift().maxbudget(budget);

		return explr;
	}

	public GoalStructure smartExploring(TestAgent agent, Pair<Integer, Tile> heuristicLocation, int budget) {

		Goal exploreG = ((PrimitiveGoal) exploring(heuristicLocation, budget)).getGoal();

		Tactic originalExploreTac = exploreG.getTactic();

		Tactic newExploreTac = FIRSTof(originalExploreTac);

		return FIRSTof(exploreG.withTactic(newExploreTac).lift());
	}

	@Override
	public GoalStructure positionInCloseRange(Pair<Integer, Tile> p) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GoalStructure entityStateRefreshed(String entityId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GoalStructure entityInteracted(String entityId) {
		// TODO Auto-generated method stub
		return null;
	}

}
