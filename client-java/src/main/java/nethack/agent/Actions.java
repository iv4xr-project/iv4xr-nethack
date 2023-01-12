package nethack.agent;

import static nl.uu.cs.aplib.AplibEDSL.*;
import static nethack.agent.Utils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.object.EntityType;
import nethack.utils.NethackSurface_NavGraph.Tile;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.utils.Pair;

public class Actions {
	static final Logger logger = LogManager.getLogger(Actions.class);
	/**
	 * Construct an action that would guide the agent to the given location.
	 */
	static Action navigateTo(int levelId, int x, int y) {
		return action("move-to").do2((AgentState S) -> (Tile nextTile) -> {
			WorldModel newwom = WorldModels.moveTo(S, nextTile);
			logger.info(String.format(">>> navigateTo %s", nextTile));
			return new Pair<>(S, newwom);
		}).on((AgentState S) -> {
			if (!S.agentIsAlive())
				return null;
			var a = S.worldmodel.elements.get(S.worldmodel().agentId);
			Tile agentPos = toTile(S.worldmodel.position);
			var path = adjustedFindPath(S, levelId(a), agentPos.x, agentPos.y, levelId, x, y);
			if (path == null) {
				return null;
			}
			// the first element is the src itself, so we need to pick the next one:
			return path.get(1).snd;
		});
	}

	/**
	 * Construct an action that would guide the agent to a tile adjacent to the
	 * target entity.
	 */
	static Action navigateTo(String targetId) {
		return action("move-to").do2((AgentState S) -> (Tile[] nextTile) -> {
			if (nextTile.length == 0) {
				logger.info("NavigateTo has no path");
				return new Pair<>(S, S.env().observe(S.worldmodel().agentId));
			}
			WorldModel newwom = WorldModels.moveTo(S, nextTile[0]);
			logger.info(String.format(">>> navigateTo %s", nextTile[0]));
			return new Pair<>(S, newwom);
		}).on((AgentState S) -> {
			// return three possible values:
			// (1) null --> the action is not enabled
			// (2) empty array of tiles --> the agent is already next to the target
			// (3) a singleton array of tile --> the next tile to move to
			//
			if (!S.agentIsAlive()) {
				System.out.print("Cannot navigate since agent is dead");
				return null;
			}
			var a = S.worldmodel.elements.get(S.worldmodel().agentId);
			Tile agentPos = toTile(S.worldmodel.position);
			WorldEntity e = S.worldmodel.elements.get(targetId);
			if (e == null) {
				System.out.print("Cannot navigate since it is nextdoor");
				return null;
			}
			Tile target = toTile(e.position);
			if (levelId(a) == levelId(e) && adjacent(agentPos, target, false)) {
				Tile[] nextTile = {};
				System.out.print("Found path");
				return nextTile;
			}
			var path = Utils.adjustedFindPath(S, levelId(a), agentPos.x, agentPos.y, levelId(e), target.x,
					target.y);
			if (path == null) {
				System.out.print("No path aparently");
				return null;
			}
			Tile[] nextTile = { path.get(1).snd };
			System.out.print("Found path");
			return nextTile;
		});
	}
	

	/**
	 * Construct an action that would interact with an entity of the given id. The
	 * action's guard is left unconstrained (so the action would always be enabled).
	 * You can use the "on" method to add a guard.
	 */
	static Action interact(String targetId) {
		return action("interact").do2((AgentState S) -> (Tile nextTile) -> {
			WorldModel newwom = WorldModels.moveTo(S, nextTile);
			logger.info(String.format(">>> interact %s", nextTile));
			return new Pair<>(S, newwom);
		});
	}


	/**
	 * Construct an action that would explore the world, in the direction of the
	 * given location.
	 */
	static Action explore(Pair<Integer, Tile> heuristicLocation) {
		Action alpha = action("explore").do2((AgentState S) -> (Tile nextTile) -> {
			WorldModel newwom = WorldModels.moveTo(S, nextTile);
			logger.info(String.format(">>> explore %s", nextTile));
			return new Pair<>(S, newwom);
		}).on((AgentState S) -> {
			if (!S.agentIsAlive())
				return null;
			var a = S.worldmodel.elements.get(S.worldmodel().agentId);
			Tile agentPos = toTile(S.worldmodel.position);
			// System.out.println(">>> agent is " + S.worldmodel().agentId) ;
			// System.out.println(">>> explore is invoked") ;
			List<Pair<Integer, Tile>> path;
			if (heuristicLocation == null) {
				// System.out.println(">>> @maze " + Utils.mazeId(a) + ", tile: " + agentPos) ;
				path = S.multiLayerNav.explore(loc3(levelId(a), agentPos.x, agentPos.y));
			} else
				path = S.multiLayerNav.explore(loc3(levelId(a), agentPos.x, agentPos.y), heuristicLocation);
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
		return alpha;
	}
	
	/**
	 * Construct an action that would attack an adjacent monster. The action is
	 * unguarded.
	 */
	static Action attackMonster() {
		return action("attack").do1((AgentState S) -> {
			var ms = S.adjacentEntities(EntityType.MONSTER, true);
			// just choose the first one:
			Tile m = toTile(ms.get(0).position);
			logger.info(String.format(">>> attackMonster %s", m));
			WorldModel newwom = WorldModels.moveTo(S, m);
			return new Pair<>(S, newwom);
		});
	}
	
	/**
	 * Construct an action that would kick the door. The action is unguarded.
	 */
	static Action kickDoor() {
		return action("kick door").do1((AgentState S) -> {
			var ms = S.adjacentEntities(EntityType.DOOR, false);
			Tile m = toTile(ms.get(0).position);
			logger.info(String.format(">>> kickDoor @%s", m));
			WorldModel newwom = WorldModels.kickDoor(S, m);
			return new Pair<>(S, newwom);
		});
	}
	
	static Action addClosedDoor() {
		return action("Add door to list").do1((AgentState S) -> {
			var doors = S.worldmodel.elements.values().stream().filter(x -> x.type == EntityType.DOOR.toString()).collect(Collectors.toList());
			doors = doors.stream().filter(d -> (boolean)d.properties.get("closed")).collect(Collectors.toList());
			doors = doors.stream().filter(d -> !TacticLib.added_closedDoors.contains(d)).collect(Collectors.toList());
			WorldEntity we = doors.get(0);
			logger.info(String.format(">>> addClosedDoor @%s", we.position));
			TacticLib.added_closedDoors.add(we);
			return new Pair<>(S, WorldModels.doNothing(S));
		});
	}
	
	static Action walkToClosedDoor() {
		// Works using Addbefore with Abort call in main goal.
		return navigateTo(TacticLib.added_closedDoors.get(0).id);
	}
	
	static Action exploreFloor() {
		// Works using Addbefore with Abort call in main goal.
		return addBefore((AgentState S) -> {
			var doors = S.worldmodel.elements.values().stream().filter(x -> x.type == EntityType.DOOR.toString()).collect(Collectors.toList());
			doors = doors.stream().filter(d -> (boolean)d.properties.get("closed")).collect(Collectors.toList());
			doors = doors.stream().filter(d -> !TacticLib.explored_added_closedDoors.contains(d)).collect(Collectors.toList());
			WorldEntity we = doors.get(0);
			TacticLib.explored_added_closedDoors.add(we);
			Tile m = toTile(we.position);
			logger.info(String.format(">>> walkToClosedDoor @%s", m));
			return goal("walk to closedDoor").toSolve((Pair<AgentState, WorldModel> proposal) -> {
				// Should return true if door is opened
				return false;
			}).withTactic(FIRSTof(
				Actions.attackMonster()
					.on_(new TacticLib().inCombat_and_hpNotCritical).lift(),
				navigateTo(we.id).lift(),
				Actions.kickDoor()
					.on_(new TacticLib().near_closedDoor).lift(),
				new TacticLib().explore(null),
				ABORT()
					)).lift();
					
		});
	}
}
