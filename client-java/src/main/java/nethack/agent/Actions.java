package nethack.agent;

import static nl.uu.cs.aplib.AplibEDSL.*;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;
import nethack.agent.navigation.NavAction;
import nethack.agent.navigation.NavTactic;
import nethack.agent.navigation.NavUtils;
import nethack.object.EntityType;
import nethack.utils.NethackSurface_NavGraph.Tile;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.utils.Pair;

public class Actions {
	static final Logger logger = LogManager.getLogger(Actions.class);
	/**
	 * Construct an action that would interact with an entity of the given id. The
	 * action's guard is left unconstrained (so the action would always be enabled).
	 * You can use the "on" method to add a guard.
	 */
	static Action interact(String targetId) {
		return action("interact").do2((AgentState S) -> (Tile nextTile) -> {
			WorldModel newwom = NavUtils.moveTo(S, nextTile);
			logger.info(String.format(">>> interact %s", nextTile));
			return new Pair<>(S, newwom);
		});
	}

	/**
	 * Construct an action that would attack an adjacent monster. The action is
	 * unguarded.
	 */
	static Action attackMonster() {
		return action("attack").do1((AgentState S) -> {
			var ms = S.adjacentEntities(EntityType.MONSTER, true);
			// just choose the first one:
			Vec3 position = ms.get(0).position;
			logger.info(String.format(">>> attackMonster %s", position));
			WorldModel newwom = NavUtils.moveTo(S, position);
			return new Pair<>(S, newwom);
		});
	}
	
	/**
	 * Construct an action that would kick the door. The action is unguarded.
	 */
	static Action kickDoor() {
		return action("kick door").do1((AgentState S) -> {
			var ms = S.adjacentEntities(EntityType.DOOR, false);
			Vec3 position = ms.get(0).position;
			logger.info(String.format(">>> kickDoor @%s", position));
			WorldModel newwom = WorldModels.kickDoor(S, position);
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
		return NavAction.navigateTo(TacticLib.added_closedDoors.get(0).id);
	}
	
	static Action exploreFloor() {
		// Works using Addbefore with Abort call in main goal.
		return addBefore((AgentState S) -> {
			var doors = S.worldmodel.elements.values().stream().filter(x -> x.type == EntityType.DOOR.toString()).collect(Collectors.toList());
			doors = doors.stream().filter(d -> (boolean)d.properties.get("closed")).collect(Collectors.toList());
			doors = doors.stream().filter(d -> !TacticLib.explored_added_closedDoors.contains(d)).collect(Collectors.toList());
			WorldEntity we = doors.get(0);
			TacticLib.explored_added_closedDoors.add(we);
			Tile m = NavUtils.toTile(we.position);
			logger.info(String.format(">>> walkToClosedDoor @%s", m));
			return goal("walk to closedDoor").toSolve((Pair<AgentState, WorldModel> proposal) -> {
				// Should return true if door is opened
				return false;
			}).withTactic(FIRSTof(
				Actions.attackMonster()
					.on_(new TacticLib().inCombat_and_hpNotCritical).lift(),
				NavAction.navigateTo(we.id).lift(),
				Actions.kickDoor()
					.on_(new TacticLib().near_closedDoor).lift(),
				NavTactic.explore(),
				ABORT()
					)).lift();
					
		});
	}
}
