package nethack.agent;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import nethack.agent.navigation.NavUtils;
import nethack.object.EntityType;
import nethack.utils.NethackSurface_NavGraph.Tile;
import nl.uu.cs.aplib.mainConcepts.Tactic;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
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
public class TacticLib {
	/**
	 * This constructs a "default" tactic to interact with an entity. The tactic is
	 * enabled if the entity is known in the agent's state/wom, and if it is
	 * adjacent to the agent.
	 */
	public Tactic interactTac(String targetId) {
		var alpha = Actions.interact(targetId).on((AgentState S) -> {
			if (!S.agentIsAlive())
				return null;
			var a = S.worldmodel.elements.get(S.worldmodel().agentId);
			Tile agentPos = NavUtils.toTile(S.worldmodel.position);
			WorldEntity e = S.worldmodel.elements.get(targetId);
			if (e == null || NavUtils.levelId(a) != NavUtils.levelId(e)) {
				return null;
			}
			Tile target = NavUtils.toTile(e.position);
			if (NavUtils.adjacent(agentPos, target, true)) {
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
		List<WorldEntity> doors = S.adjacentEntities(EntityType.DOOR, false);
		doors = doors.stream().filter(d -> (boolean)d.properties.get("closed")).collect(Collectors.toList());
		return doors.size() > 0;
	};
	
	public static List<WorldEntity> added_closedDoors = new ArrayList<WorldEntity>();
	public static List<WorldEntity> explored_added_closedDoors = new ArrayList<WorldEntity>();
	public Predicate<AgentState> exists_closedDoor = S -> {
		var doors = findOfType(S, EntityType.DOOR);
		doors = doors.stream().filter(d -> (boolean)d.properties.get("closed")).collect(Collectors.toList());
		doors = doors.stream().filter(d -> !added_closedDoors.contains(d)).collect(Collectors.toList());
		doors = doors.stream().filter(d -> !explored_added_closedDoors.contains(d)).collect(Collectors.toList());
		return doors.size() > 0;
	};
	
	public Predicate<AgentState> closed_doors_listed = S -> {
		return added_closedDoors.size() - explored_added_closedDoors.size() > 0;
	};
	
	public List<WorldEntity> findOfType(AgentState S, EntityType type) {
		return S.worldmodel.elements.values().stream().filter(x -> x.type == type.name()).collect(Collectors.toList());
	}
}
