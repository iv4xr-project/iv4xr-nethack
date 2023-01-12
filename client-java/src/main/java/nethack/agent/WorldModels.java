package nethack.agent;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;
import nethack.agent.navigation.NavUtils;
import nethack.object.Command;
import nethack.utils.NethackSurface_NavGraph.Tile;

public class WorldModels {
	static WorldModel kickDoor(AgentState state, Vec3 targetTile) {
		return kickDoor(state, NavUtils.toTile(targetTile));
	}
	
	static WorldModel kickDoor(AgentState state, Tile targetTile) {
		Tile t0 = NavUtils.toTile(state.worldmodel.position);
		if (!NavUtils.adjacent(t0, targetTile, false)) {
			throw new IllegalArgumentException("");			
		}
		state.env().action(Command.COMMAND_KICK);
		var wom = NavUtils.moveTo(state, targetTile);
		// We expect the door to be opened after the kick
		WorldEntity we = wom.elements.get(String.format("DOOR_%d_%d", targetTile.x, targetTile.y));
		we.properties.put("closed", false);
		return wom;
	}
	
	static WorldModel doNothing(AgentState state) {
		return state.env().action(Command.MISC_MORE);
	}
}
