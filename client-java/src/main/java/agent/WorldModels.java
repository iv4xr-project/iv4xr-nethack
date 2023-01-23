package agent;

import agent.navigation.NavUtils;
import agent.navigation.surface.Tile;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.enums.Command;

public class WorldModels {
  static WorldModel kickDoor(AgentState state, Tile targetTile) {
    Tile t0 = NavUtils.toTile(state.worldmodel.position);
    if (!NavUtils.adjacent(t0, targetTile, false)) {
      throw new IllegalArgumentException("");
    }
    state.env().action(Command.COMMAND_KICK);
    return NavUtils.moveTo(state, targetTile);
  }

  static WorldModel eatFood(AgentState state, char itemSlot) {
    performCommand(state, Command.COMMAND_EAT);
    Command eatCommand = Command.ADDITIONAL_ASCII;
    eatCommand.stroke = String.format("-%s", itemSlot);
    performCommand(state, eatCommand);
    return performCommand(state, Command.MISC_MORE);
  }

  static WorldModel performCommand(AgentState state, Command command) {
    return state.env().action(command);
  }

  static WorldModel doNothing(AgentState state) {
    return performCommand(state, Command.MISC_MORE);
  }
}
