package nethack;

import connection.SendCommandClient;
import java.util.Objects;
import java.util.Scanner;
import nethack.enums.Command;
import nethack.enums.GameMode;
import nethack.object.GameState;
import nethack.object.Level;
import nethack.object.Seed;
import nethack.object.StepState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetHack {
  public static final Logger netHackLogger = LogManager.getLogger(NetHackLoggers.NetHackLogger);
  public static final Logger seedLogger = LogManager.getLogger(NetHackLoggers.SeedLogger);
  public GameState gameState = new GameState();
  public GameMode gameMode;
  public Seed seed;

  SendCommandClient commander;

  public NetHack(SendCommandClient commander) {
    init(commander, GameMode.NetHackChallenge);
    reset();
  }

  public NetHack(SendCommandClient commander, Seed seed) {
    if (seed == null) {
      init(commander, GameMode.NetHackChallenge);
      reset();
    } else {
      init(commander, GameMode.NetHack);
      setSeed(seed);
    }
  }

  private void init(SendCommandClient commander, GameMode gameMode) {
    this.commander = commander;
    this.gameMode = gameMode;
    netHackLogger.info("Initialize game");
    commander.read(Object.class);
  }

  public Seed getSeed() {
    Seed seed = commander.sendCommand("Get_seed", null, Seed.class);
    seedLogger.info("Current seed: " + seed);
    return seed;
  }

  public void setSeed(Seed seed) {
    gameMode = GameMode.NetHack;
    seedLogger.info("New seed is:" + seed);
    commander.writeCommand("Set_seed", seed);
    reset();
  }

  public void reset() {
    commander.sendCommand("Reset", gameMode.toString(), Object.class);
    step(Command.MISC_MORE);
    render();
  }

  public void loop() {
    while (!gameState.done) {
      Command command = waitCommand(false);
      StepType stepType = step(command);
      if (stepType == StepType.Valid) {
        render();
      }
    }
    netHackLogger.info("GameState indicates it is done, loop stopped");
  }

  public void close() {
    netHackLogger.info("Close game");
    commander.writeCommand("Close", "");
  }

  public Level level() {
    return gameState.level();
  }

  public void render() {
    commander.writeCommand("Render", "");
    System.out.println(gameState);
  }

  public Command waitCommand(boolean acceptNoCommand) {
    // Do not close scanner, otherwise it cannot read the next command
    @SuppressWarnings("resource")
    Scanner scanner = new Scanner(System.in);
    if (acceptNoCommand) {
      System.out.print("(Optional) ");
    }
    System.out.print("Input a command: ");

    while (true) {
      String input = scanner.nextLine();
      Command command = Command.fromValue(input);
      if (command != null) {
        return command;
      } else if (Objects.equals(input, "") && acceptNoCommand) {
        return null;
      }
      System.out.print("Input \"" + input + "\" not found, enter again: ");
    }
  }

  public StepType step(Command command) {
    switch (command) {
      case COMMAND_EXTLIST:
        Command.prettyPrintActions(gameMode);
        return StepType.Special;
      case COMMAND_REDRAW:
        System.out.println(gameState);
        return StepType.Special;
      case ADDITIONAL_SHOW_SEED:
        return StepType.Special;
      case ADDITIONAL_SET_SEED:
        int index = Integer.parseInt(command.stroke.substring(1));
        setSeed(Seed.presets[index]);
        return StepType.Special;
      case ADDITIONAL_ASCII:
        char character = command.stroke.charAt(1);
        return step(Command.ADDITIONAL_ASCII, character);
      case COMMAND_INVENTORY:
        System.out.println(gameState.player.inventory);
        return StepType.Special;
      case COMMAND_INVENTTYPE:
        // Does something different actually
        System.out.println(gameState.player.inventory);
        return StepType.Special;
      default:
        break;
    }

    int index = command.getIndex(gameMode);
    if (index < 0) {
      netHackLogger.warn(
          String.format("Command: %s not available in GameMode: %s", command, gameMode));
      return StepType.Invalid;
    }

    return step(command, index);
  }

  private StepType step(Command command, int index) {
    netHackLogger.info("Command: " + command);
    StepState stepState = commander.sendCommand("step", index, StepState.class);
    updateGameState(stepState);
    return StepType.Valid;
  }

  private StepType step(Command command, char character) {
    netHackLogger.info(String.format("Command: %s %s", command, character));
    StepState stepState = commander.sendCommand("step_stroke", character, StepState.class);
    updateGameState(stepState);
    return StepType.Valid;
  }

  private void updateGameState(StepState stepState) {
    if (stepState.done) {
      netHackLogger.info("Game run terminated, step indicated: done");
      return;
    }
    // Add to world if new level is explored
    if (stepState.stats.zeroIndexLevelNumber == gameState.world.size()) {
      stepState.level.setChangedCoordinates(null);
      gameState.world.add(stepState.level);
    } else {
      stepState.level.setChangedCoordinates(
          gameState.world.get(stepState.stats.zeroIndexLevelNumber));
      gameState.world.set(stepState.stats.zeroIndexLevelNumber, stepState.level);
    }

    // Set all members to the correct values
    gameState.message = stepState.message;

    // Remeber last position of player
    if (gameState.player != null) {
      stepState.player.previousPosition = gameState.player.position;
      stepState.player.previousPosition2D = gameState.player.position2D;
    }
    gameState.player = stepState.player;
    gameState.stats = stepState.stats;
    gameState.done = stepState.done;
    gameState.info = stepState.info;
  }

  public enum StepType {
    Invalid,
    Valid,
    Special;
  }
}
