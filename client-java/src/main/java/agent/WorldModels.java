package agent;

import agent.navigation.NavUtils;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import eu.iv4xr.framework.spatial.Vec3;
import nethack.object.Command;
import agent.navigation.surface.Tile;

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
        return NavUtils.moveTo(state, targetTile);
    }

    static WorldModel performCommand(AgentState state, Command command) { return state.env().action(command); }

    static WorldModel doNothing(AgentState state) {
        return state.env().action(Command.MISC_MORE);
    }
}