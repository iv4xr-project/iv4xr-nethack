package nethack.agent.navigation;

import static nl.uu.cs.aplib.AplibEDSL.*;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.agent.AgentState;
import nethack.utils.NethackSurface_NavGraph.Tile;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.utils.Pair;

public class NavAction {
	static final Logger logger = LogManager.getLogger(NavAction.class);
	/**
	 * Construct an action that would guide the agent to the given location.
	 */
	public static Action navigateTo(int levelId, int x, int y) {
		return action("move-to").do2((AgentState S) -> (Tile nextTile) -> {
			logger.info(String.format(">>> navigateTo %s", nextTile));
			WorldModel newwom = NavUtils.moveTo(S, nextTile);
			return new Pair<>(S, newwom);
		}).on((AgentState S) -> {
			if (!S.agentIsAlive())
				return null;
			var a = S.worldmodel.elements.get(S.worldmodel().agentId);
			Tile agentPos = NavUtils.toTile(S.worldmodel.position);
			var path = NavUtils.adjustedFindPath(S, NavUtils.levelId(a), agentPos.x, agentPos.y, levelId, x, y);
			if (path == null) {
				return null;
			}
			// the first element is the src itself, so we need to pick the next one:
			return path.get(1).snd;
		});
	}

	// Construct an action that would guide the agent to to the target entity.
	public static Action navigateTo(String targetId) {
		return action("move-to").do2((AgentState S) -> (Tile nextTile) -> {
			logger.info(String.format(">>> navigateTo %s", nextTile));
			WorldModel newwom = NavUtils.moveTo(S, nextTile);
			return new Pair<>(S, newwom);
		}).on((AgentState S) -> {
			// return three possible values:
			// (1) null --> the action is not enabled
			// (2 disabled) empty array of tiles --> the agent is already next to the target
			// (3) a singleton array of tile --> the next tile to move to
			//
			if (!S.agentIsAlive()) {
				System.out.print("Cannot navigate since agent is dead");
				return null;
			}
			var a = S.worldmodel.elements.get(S.worldmodel().agentId);
			Tile agentPos = NavUtils.toTile(S.worldmodel.position);
			WorldEntity e = S.worldmodel.elements.get(targetId);
			if (e == null) {
				System.out.print("Cannot navigate since it is nextdoor");
				return null;
			}
			Tile target = NavUtils.toTile(e.position);
//			if (NavUtils.levelId(a) == NavUtils.levelId(e) && NavUtils.adjacent(agentPos, target, false)) {
//				Tile[] nextTile = {};
//				System.out.print("Found path");
//				return nextTile;
//			}
			var path = NavUtils.adjustedFindPath(S, NavUtils.levelId(a), agentPos.x, agentPos.y, NavUtils.levelId(e), target.x,
					target.y);
			if (path == null) {
				System.out.print("No path aparently");
				return null;
			}
			System.out.print("Found path");
			return path.get(1).snd;
		});
	}
	
	// Construct an action that would guide the agent to to the target entity.
	public static Action navigateNextTo(String targetId, boolean allowDiagonally) {
		return action("move-to").do2((AgentState S) -> (Tile nextTile) -> {
			logger.info(String.format(">>> navigateNextTo %s", nextTile));
			WorldModel newwom = NavUtils.moveTo(S, nextTile);
			return new Pair<>(S, newwom);
		}).on((AgentState S) -> {
			// return three possible values:
			// (1) null --> the action is not enabled
			// (2 disabled) empty array of tiles --> the agent is already next to the target
			// (3) a singleton array of tile --> the next tile to move to
			//
			if (!S.agentIsAlive()) {
				System.out.print("Cannot navigate since agent is dead");
				return null;
			}
			var a = S.worldmodel.elements.get(S.worldmodel().agentId);
			Tile agentPos = NavUtils.toTile(S.worldmodel.position);
			WorldEntity e = S.worldmodel.elements.get(targetId);
			if (e == null) {
				System.out.println("Cannot navigate since it is nextdoor");
				return null;
			}
			Tile target = NavUtils.toTile(e.position);
			if (S.nextToEntity(targetId, allowDiagonally)) {
				System.out.println(String.format("Next to item id:%s", targetId));
				return null;
			}
//			if (NavUtils.levelId(a) == NavUtils.levelId(e) && NavUtils.adjacent(agentPos, target, false)) {
//				Tile[] nextTile = {};
//				System.out.print("Found path");
//				return nextTile;
//			}
			var path = NavUtils.adjustedFindPath(S, NavUtils.levelId(a), agentPos.x, agentPos.y, NavUtils.levelId(e), target.x,
					target.y);
			if (path == null) {
				System.out.println("No path aparently");
				return null;
			}
			System.out.println("Found path");
			return path.get(1).snd;
		});
	}


	/**
	 * Construct an action that would explore the world, in the direction of the
	 * given location.
	 */
	public static Action explore(Pair<Integer, Tile> heuristicLocation) {
		return action("explore").do2((AgentState S) -> (Tile nextTile) -> {
			WorldModel newwom = NavUtils.moveTo(S, nextTile);
			logger.info(String.format(">>> explore %s", nextTile));
			return new Pair<>(S, newwom);
		}).on((AgentState S) -> {
			if (!S.agentIsAlive())
				return null;
			var a = S.worldmodel.elements.get(S.worldmodel().agentId);
			Tile agentPos = NavUtils.toTile(S.worldmodel.position);
			// System.out.println(">>> agent is " + S.worldmodel().agentId) ;
			// System.out.println(">>> explore is invoked") ;
			List<Pair<Integer, Tile>> path;
			if (heuristicLocation == null) {
				// System.out.println(">>> @maze " + Utils.mazeId(a) + ", tile: " + agentPos) ;
				path = S.multiLayerNav.explore(NavUtils.loc3(NavUtils.levelId(a), agentPos.x, agentPos.y));
			} else
				path = S.multiLayerNav.explore(NavUtils.loc3(NavUtils.levelId(a), agentPos.x, agentPos.y), heuristicLocation);
			if (path == null) {
				// System.out.println(">>>> can't find an explore path!") ;
				return null;
			}
			try {
				return path.get(1).snd;
			} catch (Exception e) {
				logger.debug(String.format("agent @%s nothing left to explore", agentPos));
				throw e;
			}
			
			// return path.get(1).snd ;
		});
	}
}
