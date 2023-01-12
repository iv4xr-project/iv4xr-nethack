package nethack.agent.navigation;

import nethack.agent.AgentState;
import nethack.utils.NethackSurface_NavGraph.Tile;
import nl.uu.cs.aplib.mainConcepts.SimpleState;
import nl.uu.cs.aplib.mainConcepts.Tactic;
import nl.uu.cs.aplib.utils.Pair;


public class NavTactic {
	public static Tactic navigateTo(Pair<Integer, Tile> location) {
		return NavAction.navigateTo(location.fst, location.snd.x, location.snd.y).lift();
	}

	// Construct a tactic that would guide the agent to a tile to the target entity.
	public static Tactic navigateTo(String targetId) {
		return NavAction.navigateTo(targetId).lift();
	}
	
	// Construct a tactic that would guide the agent to a tile adjacent to the location.
	public static Tactic navigateNextTo(Pair<Integer, Tile> location, boolean allowDiagonally) {
		return NavAction.navigateTo(location.fst, location.snd.x, location.snd.y).on_((AgentState S) -> {
			var player = S.worldmodel.elements.get(S.worldmodel.agentId);
			Tile p = NavUtils.toTile(player.position);
			return !NavUtils.adjacent(p, location.snd, allowDiagonally);
		}).lift();
	}

	// Construct a tactic that would guide the agent to a tile adjacent to the target entity.
	public static Tactic navigateNextTo(String targetId, boolean allowDiagonally) {
		return NavAction.navigateNextTo(targetId, allowDiagonally).lift();
		//return NavAction.navigateTo(targetId).on_((AgentState S) -> !S.nextToEntity(targetId, allowDiagonally)).lift();
	}

	public static boolean exploringDone(SimpleState S) {
		return !NavAction.explore(null).isEnabled(S);
	}
	
	// Explore version that tries to further explore the level
	public static Tactic explore() {
		return NavTactic.explore(null);
	}

	// Explores to a given location, if location is null then it explores new parts of the level
	public static Tactic explore(Pair<Integer, Tile> heuristicLocation) {
		return NavAction.explore(heuristicLocation).lift();
	}
}
