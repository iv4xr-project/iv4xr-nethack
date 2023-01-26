package agent.strategy;

import agent.iv4xr.AgentState;
import agent.navigation.NavUtils;
import agent.navigation.surface.Tile;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.enums.Command;
import nl.uu.cs.aplib.utils.Pair;

public class WorldModels {
  static WorldModel kickDoor(AgentState state, Pair<Integer, Tile> targetTile) {
    Tile t0 = NavUtils.toTile(state.worldmodel.position);
    assert NavUtils.adjacent(t0, targetTile.snd, false);
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
