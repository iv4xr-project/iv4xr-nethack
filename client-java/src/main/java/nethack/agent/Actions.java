package nethack.agent;

import static nl.uu.cs.aplib.AplibEDSL.*;

import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;
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

	// Construct an action that would attack an adjacent monster.
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
	
	// Construct an action that would kick the door.
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
			WorldEntity we = doors.get(0);
			Predicates.closed_door = doors.get(0);
			logger.info(String.format(">>> addClosedDoor @%s", we.position));
			return new Pair<>(S, WorldModels.doNothing(S));
		});
	}
	
	static Action openClosedDoor() {
		// Works using Addbefore with Abort call in main goal.
		return addBefore((AgentState S) -> {
			WorldEntity we = Predicates.closed_door;
			Tile m = NavUtils.toTile(we.position);
			logger.info(String.format(">>> openClosedDoor @%s", m));
			return goal("open closed door").toSolve((Pair<AgentState, WorldModel> proposal) -> {
				// Should return true if door is opened
				return false;
			}).withTactic(FIRSTof(
				Actions.attackMonster()
					.on_(Predicates.inCombat_and_hpNotCritical).lift(),
				NavTactic.navigateNextTo(we.id, false),
				SEQ(Actions.kickDoor()
					.on_(Predicates.near_closedDoor).lift(), ABORT()),
				NavTactic.explore(),
				ABORT()
				)).lift();
		});
	}
}
