package nethack.agent;

import static nl.uu.cs.aplib.AplibEDSL.*;
import static nethack.agent.Utils.*;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.object.EntityType;
import nethack.utils.NethackSurface_NavGraph.Tile;
import nl.uu.cs.aplib.Logging;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.utils.Pair;

public class Actions {
	static Logger logger = Logging.getAPLIBlogger();
	/**
	 * Construct an action that would guide the agent to the given location.
	 */
	static Action navigateTo(int levelId, int x, int y) {
		return action("move-to").do2((AgentState S) -> (Tile nextTile) -> {
			WorldModel newwom = WorldModels.moveTo(S, nextTile);
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
				return new Pair<>(S, S.env().observe(S.worldmodel().agentId));
			}
			WorldModel newwom = WorldModels.moveTo(S, nextTile[0]);
			return new Pair<>(S, newwom);
		}).on((AgentState S) -> {
			// return three possible values:
			// (1) null --> the action is not enabled
			// (2) empty array of tiles --> the agent is already next to the target
			// (3) a singleton array of tile --> the next tile to move to
			//
			if (!S.agentIsAlive())
				return null;
			var a = S.worldmodel.elements.get(S.worldmodel().agentId);
			Tile agentPos = toTile(S.worldmodel.position);
			WorldEntity e = S.worldmodel.elements.get(targetId);
			if (e == null) {
				return null;
			}
			Tile target = toTile(e.position);
			if (levelId(a) == levelId(e) && adjacent(agentPos, target, false)) {
				Tile[] nextTile = {};
				return nextTile;
			}
			var path = Utils.adjustedFindPath(S, levelId(a), agentPos.x, agentPos.y, levelId(e), target.x,
					target.y);
			if (path == null) {
				return null;
			}
			Tile[] nextTile = { path.get(1).snd };
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
				System.out.println(">>> agent @" + agentPos + ", path: " + path);
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
			logger.info(">>> " + S.worldmodel.agentId + " attacks " + m);
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
			// just choose the first one:
			Tile m = toTile(ms.get(0).position);
			logger.info(">>> " + S.worldmodel.agentId + " kicks door " + m);
			WorldModel newwom = WorldModels.kickDoor(S, m);
			return new Pair<>(S, newwom);
		});
	}
	
	static Action walkToClosedDoor() {
		return addBefore((AgentState S) -> { 
			var doors = S.worldmodel.elements.values().stream().filter(x -> x.type == EntityType.DOOR.toString()).collect(Collectors.toList());
			doors = doors.stream().filter(d -> (boolean)d.properties.get("closed")).collect(Collectors.toList());
			doors = doors.stream().filter(d -> !TacticLib.added_closedDoors.contains(d.id)).collect(Collectors.toList());
			Tile m = toTile(doors.get(0).position);
			TacticLib.added_closedDoors.add(doors.get(0).id);
			return SEQ(new GoalLib().entityInCloseRange(doors.get(0).id));
		}).on((AgentState S) -> { return "s"; });
	}
}
