package agent.strategy;

import static nethack.enums.CommandEnum.*;

import agent.iv4xr.AgentState;
import agent.navigation.hpastar.smoother.Direction;
import eu.iv4xr.framework.mainConcepts.WorldModel;
import java.util.List;
import nethack.object.Command;
import nethack.object.Entity;
import nethack.object.Player;
import nethack.object.items.Item;

public class WorldModels {
  public static WorldModel<Player, Entity> kick(AgentState state, Direction direction) {
    Command command = Direction.getCommand(direction);
    return performCommands(state, List.of(new Command(COMMAND_KICK), command));
  }

  public static WorldModel<Player, Entity> open(AgentState state, Direction direction) {
    Command command = Direction.getCommand(direction);
    return performCommands(state, List.of(new Command(COMMAND_OPEN), command));
  }

  public static WorldModel<Player, Entity> forceAttack(AgentState state, Direction direction) {
    Command command = Direction.getCommand(direction);
    return performCommands(state, List.of(new Command(COMMAND_FIGHT), command));
  }

  public static WorldModel<Player, Entity> fire(AgentState state, Direction direction) {
    Command command = Direction.getCommand(direction);
    return performCommands(state, List.of(new Command(COMMAND_FIRE), command));
  }

  public static WorldModel<Player, Entity> throwDagger(
      AgentState state, Item item, Direction direction) {
    Command command = Direction.getCommand(direction);
    return performCommands(
        state, List.of(new Command(COMMAND_THROW), new Command(item.symbol), command));
  }

  public static WorldModel<Player, Entity> eatItem(AgentState state, char itemSlot) {
    Command itemCommand = Command.fromLiteralStroke(String.valueOf(itemSlot));
    return performCommands(
        state, List.of(new Command(COMMAND_EAT), itemCommand, new Command(MISC_MORE)));
  }

  public static WorldModel<Player, Entity> quaffItem(AgentState state, char itemSlot) {
    Command itemCommand = Command.fromLiteralStroke(String.valueOf(itemSlot));
    return performCommands(
        state, List.of(new Command(COMMAND_QUAFF), itemCommand, new Command(MISC_MORE)));
  }

  public static WorldModel<Player, Entity> zapWand(AgentState state, char itemSlot) {
    Command itemCommand = Command.fromLiteralStroke(String.valueOf(itemSlot));
    return performCommands(state, List.of(new Command(COMMAND_ZAP), itemCommand));
  }

  public static WorldModel<Player, Entity> performCommands(
      AgentState state, List<Command> commands) {
    assert !commands.isEmpty();
    WorldModel<Player, Entity> worldModel = state.env().commands(commands);
    state.worldmodel.mergeNewObservation(worldModel);
    return state.worldmodel;
  }
}
