package agent.strategy;

import static nethack.enums.CommandEnum.*;

import agent.iv4xr.AgentState;
import agent.navigation.hpastar.smoother.Direction;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.object.Command;

public class WorldModels {
  static WorldModel kick(AgentState state, Direction direction) {
    Command command = Direction.getCommand(direction);
    return performCommands(state, new Command(COMMAND_KICK), command);
  }

  static WorldModel eatItem(AgentState state, char itemSlot) {
    Command itemCommand = Command.fromStroke(String.valueOf(itemSlot));
    return performCommands(state, new Command(COMMAND_EAT), itemCommand, new Command(MISC_MORE));
  }

  static WorldModel quaffItem(AgentState state, char itemSlot) {
    Command itemCommand = Command.fromStroke(String.valueOf(itemSlot));
    return performCommands(state, new Command(COMMAND_QUAFF), itemCommand, new Command(MISC_MORE));
  }

  static WorldModel zapWand(AgentState state, char itemSlot) {
    Command itemCommand = Command.fromStroke(String.valueOf(itemSlot));
    return performCommands(state, new Command(COMMAND_ZAP), itemCommand);
  }

  static WorldModel performCommands(AgentState state, Command... commands) {
    assert commands.length != 0;
    for (int i = 0; i < commands.length - 1; i++) {
      state.env().command(commands[i]);
    }
    return state.env().command(commands[commands.length - 1]);
  }

  static WorldModel doNothing(AgentState state) {
    return performCommands(state, new Command(MISC_MORE));
  }
}
