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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
	Logger logger = Logging.getAPLIBlogger();

	@Override
	public Tactic navigateToTac(Pair<Integer, Tile> location) {
		return Actions.navigateTo(location.fst, location.snd.x, location.snd.y).lift();
	}

	/**
	 * Construct a tactic that would guide the agent to a tile adjacent to the
	 * target entity.
	 */
	@Override
	public Tactic navigateToTac(String targetId) {
		return Actions.navigateTo(targetId).lift();
	}

	/**
	 * This constructs a "default" tactic to interact with an entity. The tactic is
	 * enabled if the entity is known in the agent's state/wom, and if it is
	 * adjacent to the agent.
	 */
	@Override
	public Tactic interactTac(String targetId) {
		var alpha = Actions.interact(targetId).on((AgentState S) -> {
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
	
	static List<String> added_closedDoors = new ArrayList<String>();
	public Predicate<AgentState> exists_closedDoor = S -> {
		var doors = S.worldmodel.elements.values().stream().filter(x -> x.type == EntityType.DOOR.toString()).collect(Collectors.toList());
		doors = doors.stream().filter(d -> (boolean)d.properties.get("closed")).collect(Collectors.toList());
		doors = doors.stream().filter(d -> !added_closedDoors.contains(d.id)).collect(Collectors.toList());
		return doors.size() > 0;
	};
	
	@Override
	public boolean explorationExhausted(SimpleState S) {
		return !Actions.explore(null).isEnabled(S) ;
	}

	@Override
	public Tactic explore(Pair<Integer, Tile> heuristicLocation) {
		return Actions.explore(heuristicLocation).lift();
	}
}
