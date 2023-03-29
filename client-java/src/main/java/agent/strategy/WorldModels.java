package agent.strategy;

import static nethack.enums.CommandEnum.*;

import agent.iv4xr.AgentState;
import agent.navigation.hpastar.smoother.Direction;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import nethack.object.Command;

public class WorldModels {
  public static WorldModel kick(AgentState state, Direction direction) {
    Command command = Direction.getCommand(direction);
    return performCommands(state, new Command(COMMAND_KICK), command);
  }

  public static WorldModel open(AgentState state, Direction direction) {
    Command command = Direction.getCommand(direction);
    return performCommands(state, new Command(COMMAND_OPEN), command);
  }

  public static WorldModel forceAttack(AgentState state, Direction direction) {
    Command command = Direction.getCommand(direction);
    return performCommands(state, new Command(COMMAND_FIGHT), command);
  }

  public static WorldModel fire(AgentState state, Direction direction) {
    Command command = Direction.getCommand(direction);
    return performCommands(state, new Command(COMMAND_FIRE), command);
  }

  public static WorldModel eatItem(AgentState state, char itemSlot) {
    Command itemCommand = Command.fromLiteralStroke(String.valueOf(itemSlot));
    return performCommands(state, new Command(COMMAND_EAT), itemCommand, new Command(MISC_MORE));
  }

  public static WorldModel quaffItem(AgentState state, char itemSlot) {
    Command itemCommand = Command.fromLiteralStroke(String.valueOf(itemSlot));
    return performCommands(state, new Command(COMMAND_QUAFF), itemCommand, new Command(MISC_MORE));
  }

  public static WorldModel zapWand(AgentState state, char itemSlot) {
    Command itemCommand = Command.fromLiteralStroke(String.valueOf(itemSlot));
    return performCommands(state, new Command(COMMAND_ZAP), itemCommand);
  }

  public static WorldModel performCommands(AgentState state, Command... commands) {
    assert commands.length != 0;
    WorldModel worldModel = state.env().commands(commands);
    state.worldmodel.mergeNewObservation(worldModel);
    return state.worldmodel;
  }

  public static WorldModel doNothing(AgentState state) {
    return performCommands(state, new Command(MISC_MORE));
  }
}
