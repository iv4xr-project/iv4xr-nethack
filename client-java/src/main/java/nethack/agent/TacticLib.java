package nethack.agent;

import eu.iv4xr.framework.goalsAndTactics.IInteractiveWorldTacticLib;
import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.object.Command;
import nethack.object.EntityType;
import nethack.utils.NethackSurface_NavGraph.Tile;
import nl.uu.cs.aplib.Logging;
import nl.uu.cs.aplib.mainConcepts.Action;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import nl.uu.cs.aplib.utils.Pair;

import static nl.uu.cs.aplib.AplibEDSL.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Provide several basic actions and tactics.
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
public class TacticLib implements IInteractiveWorldTacticLib<Pair<Integer, Tile>> {

	/**
	 * Distance in terms of path-length from the agent that owns S to the entity e.
	 * It uses adjustedFindPath to calculate the path.
	 */
	static int distTo(AgentState S, WorldEntity e) {
		var player = S.worldmodel.elements.get(S.worldmodel.agentId);
		Tile p = Utils.toTile(player.position);
		Tile target = Utils.toTile(e.position);
		var path = TacticLib.adjustedFindPath(S, Utils.levelId(player), p.x, p.y, Utils.levelId(e), target.x, target.y);
		if (path == null)
			return Integer.MAX_VALUE;
		return path.size() - 1;
	}

	/**
	 * Calculate a path from (x0,y0) in maze-0 to (x1,y1) in maze-1. The method will
	 * pretend that the source (x0,y0) and destination (x1,y1) are non-blocking
	 * (even if they are, e.g. if one of them is an occupied tile).
	 */
	public static List<Pair<Integer, Tile>> adjustedFindPath(AgentState state, int level0, int x0, int y0, int level1,
			int x1, int y1) {
		var nav = state.multiLayerNav;
		boolean srcOriginalBlockingState = nav.isBlocking(Utils.loc3(level0, x0, y0));
		boolean destOriginalBlockingState = nav.isBlocking(Utils.loc3(level1, x1, y1));
		nav.toggleBlockingOff(Utils.loc3(level0, x0, y0));
		nav.toggleBlockingOff(Utils.loc3(level1, x1, y1));
		var path = nav.findPath(Utils.loc3(level0, x0, y0), Utils.loc3(level1, x1, y1));
		nav.setBlockingState(Utils.loc3(level0, x0, y0), srcOriginalBlockingState);
		nav.setBlockingState(Utils.loc3(level1, x1, y1), destOriginalBlockingState);
		return path;
	}

	Logger logger = Logging.getAPLIBlogger();

	WorldModel moveTo(AgentState state, Tile targetTile) {
		Tile t0 = Utils.toTile(state.worldmodel.position);
		if (!Utils.adjacent(t0, targetTile, true))
			throw new IllegalArgumentException("");
		Command command = null;
		if (targetTile.y > t0.y) {
			if (targetTile.x > t0.x) {
				command = Command.DIRECTION_SE;
			} else if (targetTile.x < t0.x) {
				command = Command.DIRECTION_SW;
			} else {
				command = Command.DIRECTION_S;
			}
		} else if (targetTile.y < t0.y) {
			if (targetTile.x > t0.x) {
				command = Command.DIRECTION_NE;
			} else if (targetTile.x < t0.x) {
				command = Command.DIRECTION_NW;
			} else {
				command = Command.DIRECTION_N;
			}
		} else if (targetTile.x > t0.x) {
			command = Command.DIRECTION_E;
		} else {
			command = Command.DIRECTION_W;
		}
		var wom = state.env().action(command);
		return wom;
	}

	WorldModel kickDoor(AgentState state, Tile targetTile) {
		Tile t0 = Utils.toTile(state.worldmodel.position);
		if (!Utils.adjacent(t0, targetTile, false))
			throw new IllegalArgumentException("");
		Command command = Command.COMMAND_KICK;
		var wom = state.env().action(command);
		
		if (targetTile.y > t0.y) {
			command = Command.DIRECTION_S;
		} else if (targetTile.y < t0.y) {
			command = Command.DIRECTION_N;
		} else if (targetTile.x > t0.x) {
			command = Command.DIRECTION_E;
		} else {
			command = Command.DIRECTION_W;
		}
		
		// We expect the door to be opened after the kick
		WorldEntity we = wom.elements.get(String.format("DOOR_%d_%d", targetTile.x, targetTile.y));
		we.properties.put("closed", false);
		
		wom = state.env().action(command);
		return wom;
	}

	/**
	 * Construct an action that would guide the agent to the given location.
	 */
	Action navigateToAction(int levelId, int x, int y) {
		return action("move-to").do2((AgentState S) -> (Tile nextTile) -> {
			WorldModel newwom = moveTo(S, nextTile);
			return new Pair<>(S, newwom);
		}).on((AgentState S) -> {
			if (!S.agentIsAlive())
				return null;
			var a = S.worldmodel.elements.get(S.worldmodel().agentId);
			Tile agentPos = Utils.toTile(S.worldmodel.position);
			var path = adjustedFindPath(S, Utils.levelId(a), agentPos.x, agentPos.y, levelId, x, y);
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
	Action navigateToAction(String targetId) {
		return action("move-to").do2((AgentState S) -> (Tile[] nextTile) -> {
			if (nextTile.length == 0) {
				return new Pair<>(S, S.env().observe(S.worldmodel().agentId));
			}
			WorldModel newwom = moveTo(S, nextTile[0]);
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
			Tile agentPos = Utils.toTile(S.worldmodel.position);
			WorldEntity e = S.worldmodel.elements.get(targetId);
			if (e == null) {
				return null;
			}
			Tile target = Utils.toTile(e.position);
			if (Utils.levelId(a) == Utils.levelId(e) && Utils.adjacent(agentPos, target, false)) {
				Tile[] nextTile = {};
				return nextTile;
			}
			var path = adjustedFindPath(S, Utils.levelId(a), agentPos.x, agentPos.y, Utils.levelId(e), target.x,
					target.y);
			if (path == null) {
				return null;
			}
			Tile[] nextTile = { path.get(1).snd };
			return nextTile;
		});
	}

	@Override
	public Tactic navigateToTac(Pair<Integer, Tile> location) {
		return navigateToAction(location.fst, location.snd.x, location.snd.y).lift();
	}

	/**
	 * Construct a tactic that would guide the agent to a tile adjacent to the
	 * target entity.
	 */
	@Override
	public Tactic navigateToTac(String targetId) {
		return navigateToAction(targetId).lift();
	}

	/**
	 * Construct an action that would interact with an entity of the given id. The
	 * action's guard is left unconstrained (so the action would always be enabled).
	 * You can use the "on" method to add a guard.
	 */
	Action interactAction(String targetId) {
		return action("interact").do2((AgentState S) -> (Tile nextTile) -> {
			WorldModel newwom = moveTo(S, nextTile);
			return new Pair<>(S, newwom);
		});
	}

	/**
	 * This constructs a "default" tactic to interact with an entity. The tactic is
	 * enabled if the entity is known in the agent's state/wom, and if it is
	 * adjacent to the agent.
	 */
	@Override
	public Tactic interactTac(String targetId) {
		var alpha = interactAction(targetId).on((AgentState S) -> {
			if (!S.agentIsAlive())
				return null;
			var a = S.worldmodel.elements.get(S.worldmodel().agentId);
			Tile agentPos = Utils.toTile(S.worldmodel.position);
			WorldEntity e = S.worldmodel.elements.get(targetId);
			if (e == null || Utils.levelId(a) != Utils.levelId(e)) {
				return null;
			}
			Tile target = Utils.toTile(e.position);
			if (Utils.adjacent(agentPos, target, true)) {
				return target;
			}
			return null;
		});
		return alpha.lift();
	}

	public Predicate<AgentState> inCombat_and_hpNotCritical = S -> {
		var player = S.worldmodel.elements.get(S.worldmodel.agentId);
		int hp = (int) player.properties.get("hp");
		
		System.out.print("Type: ");
		for (WorldEntity we: S.worldmodel.elements.values()) {
			System.out.print(we.type + " ");
		}
		System.out.println();
		
		return hp > 5 && S.adjacentEntities(EntityType.MONSTER, true).size() > 0;
	};
	
	public Predicate<AgentState> near_closedDoor = S -> {
		var player = S.worldmodel.elements.get(S.worldmodel.agentId);
		List<WorldEntity> doors = S.adjacentEntities(EntityType.DOOR, false);
		doors = doors.stream().filter(d -> (boolean)d.properties.get("closed")).collect(Collectors.toList());
		return doors.size() > 0;
	};

	@Override
	public boolean explorationExhausted(SimpleState S) {
		return !exploreAction(null).isEnabled(S);
	}

	/**
	 * Construct an action that would explore the world, in the direction of the
	 * given location.
	 */
	Action exploreAction(Pair<Integer, Tile> heuristicLocation) {
		Action alpha = action("explore").do2((AgentState S) -> (Tile nextTile) -> {
			WorldModel newwom = moveTo(S, nextTile);
			return new Pair<>(S, newwom);
		}).on((AgentState S) -> {
			if (!S.agentIsAlive())
				return null;
			var a = S.worldmodel.elements.get(S.worldmodel().agentId);
			Tile agentPos = Utils.toTile(S.worldmodel.position);
			// System.out.println(">>> agent is " + S.worldmodel().agentId) ;
			// System.out.println(">>> explore is invoked") ;
			List<Pair<Integer, Tile>> path;
			if (heuristicLocation == null) {
				// System.out.println(">>> @maze " + Utils.mazeId(a) + ", tile: " + agentPos) ;
				path = S.multiLayerNav.explore(Utils.loc3(Utils.levelId(a), agentPos.x, agentPos.y));
			} else
				path = S.multiLayerNav.explore(Utils.loc3(Utils.levelId(a), agentPos.x, agentPos.y), heuristicLocation);
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

	@Override
	public Tactic explore(Pair<Integer, Tile> heuristicLocation) {
		return exploreAction(heuristicLocation).lift();
	}

	/**
	 * Construct an action that would attack an adjacent monster. The action is
	 * unguarded.
	 */
	Action attackMonsterAction() {
		return action("attack").do1((AgentState S) -> {
			var ms = S.adjacentEntities(EntityType.MONSTER, true);
			// just choose the first one:
			Tile m = Utils.toTile(ms.get(0).position);
			logger.info(">>> " + S.worldmodel.agentId + " attacks " + m);
			WorldModel newwom = moveTo(S, m);
			return new Pair<>(S, newwom);
		});
	}
	
	/**
	 * Construct an action that would attack an adjacent monster. The action is
	 * unguarded.
	 */
	Action kickDoorAction() {
		return action("kick door").do1((AgentState S) -> {
			var ms = S.adjacentEntities(EntityType.DOOR, false);
			// just choose the first one:
			Tile m = Utils.toTile(ms.get(0).position);
			logger.info(">>> " + S.worldmodel.agentId + " kicks door " + m);
			WorldModel newwom = kickDoor(S, m);
			return new Pair<>(S, newwom);
		});
	}
}
