package nethack;

import connection.SocketClient;
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

  SocketClient client;

  public NetHack(SocketClient commander) {
    init(commander, GameMode.NetHackChallenge);
    reset();
  }

  public NetHack(SocketClient client, Seed seed) {
    if (seed == null) {
      init(client, GameMode.NetHackChallenge);
      reset();
    } else {
      init(client, GameMode.NetHack);
      setSeed(seed);
    }
  }

  private void init(SocketClient client, GameMode gameMode) {
    this.client = client;
    this.gameMode = gameMode;
    netHackLogger.info("Initialize game");
    client.readObservationMessage();
  }

  public Seed getSeed() {
    Seed seed = client.sendGetSeed();
    seedLogger.info("Current seed: " + seed);
    return seed;
  }

  public void setSeed(Seed seed) {
    gameMode = GameMode.NetHack;
    seedLogger.info("New seed is:" + seed);
    client.sendSetSeed(seed);
    reset();
  }

  public void reset() {
    client.sendReset(gameMode.toString());
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
    client.sendClose();
  }

  public Level level() {
    return gameState.getLevel();
  }

  public void render() {
    client.sendRender();
    System.out.println(gameState);
  }

  public Command waitCommand(boolean acceptNoCommand) {
    // Do not close scanner, otherwise it cannot read the next command
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
      case ADDITIONAL_SHOW_VERBOSE:
        System.out.print(gameState.verbose());
        return StepType.Special;
      case ADDITIONAL_SHOW_SEED:
        Seed seed = client.sendGetSeed();
        seedLogger.info(seed);
        return StepType.Special;
      case ADDITIONAL_ASCII:
        char character = command.stroke.charAt(1);
        return step(Command.ADDITIONAL_ASCII, character);
      case COMMAND_INVENTORY:
      case COMMAND_INVENTTYPE: // Should do something differently
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
    StepState stepState = client.sendStep(index);
    updateGameState(stepState);
    return StepType.Valid;
  }

  private StepType step(Command command, char character) {
    netHackLogger.info(String.format("Command: %s %s", command, character));
    StepState stepState = client.sendStepStroke(character);
    updateGameState(stepState);
    return StepType.Valid;
  }

  private void updateGameState(StepState stepState) {
    if (stepState.done) {
      netHackLogger.info("Game run terminated, step indicated: done");
      return;
    }

    // Need to set new stats before setting the level
    gameState.stats = stepState.stats;
    gameState.setLevel(stepState.level);

    // Set all members to the correct values
    gameState.message = stepState.message;
    // Remember last position of player
    if (gameState.player != null) {
      stepState.player.previousPosition = gameState.player.position;
      stepState.player.previousPosition2D = gameState.player.position2D;
    }
    gameState.player = stepState.player;
    assert gameState.player.position.z == 0
        : "Before this the levelNr is not known, assert is isnt set already";
    gameState.player.position.z = gameState.getLevelNr();
    gameState.done = stepState.done;
    gameState.info = stepState.info;
  }

  public enum StepType {
    Invalid,
    Valid,
    Special;
  }
}
