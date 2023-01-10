package nethack.agent;

import eu.iv4xr.framework.mainConcepts.WorldEntity;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.object.Command;
import nethack.utils.NethackSurface_NavGraph.Tile;

public class WorldModels {
	static WorldModel moveTo(AgentState state, Tile targetTile) {
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

	static WorldModel kickDoor(AgentState state, Tile targetTile) {
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
}
