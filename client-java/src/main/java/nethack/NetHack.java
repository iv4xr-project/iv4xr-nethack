package nethack;

import connection.SocketClient;
import connection.messageencoder.Encoder;
import java.util.*;
import java.util.stream.Collectors;
import nethack.enums.CommandEnum;
import nethack.enums.GameMode;
import nethack.object.*;
import util.Loggers;
import util.Stopwatch;

public class NetHack {
  public final GameState gameState = new GameState();
  public GameMode gameMode;
  public Seed seed;
  SocketClient client;

  public NetHack(SocketClient client) {
    init(client, GameMode.NetHackChallenge);
    reset();
  }

  public NetHack(SocketClient client, Seed seed) {
    Loggers.NetHackLogger.info("Initialize game");
    if (seed == null) {
      init(client, GameMode.NetHackChallenge);
      reset();
    } else {
      init(client, GameMode.NetHack);
      System.out.printf("Init game with seed: %s%n", seed);
      setSeed(seed);
    }
  }

  private void init(SocketClient client, GameMode gameMode) {
    this.client = client;
    this.gameMode = gameMode;
  }

  public Seed getSeed() {
    Seed seed = client.sendGetSeed();
    Loggers.SeedLogger.info("Current seed: " + seed);
    return seed;
  }

  public void setSeed(Seed seed) {
    gameMode = GameMode.NetHack;
    Loggers.SeedLogger.info("New seed is:" + seed);
    client.sendSetSeed(seed);
    reset();
  }

  public void reset() {
    client.sendReset(gameMode.toString());
    step(new Command(CommandEnum.MISC_MORE));
    render();
  }

  public void loop() {
    while (!gameState.done) {
      List<Command> commands = waitCommands(false);
      StepType stepType = step(commands.toArray(new Command[] {}));
      if (stepType == StepType.Terminated) {
        break;
      } else if (stepType == StepType.Valid) {
        render();
      }
    }
    Loggers.NetHackLogger.info("GameState indicates it is done, loop stopped");
  }

  public void close() {
    Loggers.NetHackLogger.info("Close game");
    client.sendClose();
  }

  public Level level() {
    return gameState.getLevel();
  }

  public void render() {
    client.sendRender();
    System.out.println(gameState);
  }

  public List<Command> waitCommands(boolean acceptNoCommand) {
    // Do not close scanner, otherwise it cannot read the next command
    Scanner scanner = new Scanner(System.in);
    String prompt = "";
    if (acceptNoCommand) {
      prompt += "(Optional) ";
    }
    prompt += "Input a command: ";
    System.out.print(prompt);

    readCommand:
    while (true) {
      String line = scanner.nextLine();
      String[] inputs = line.split(" ");
      List<Command> commands = new ArrayList<>();
      for (String input : inputs) {
        Command command = Command.fromStroke(input);
        if (command != null) {
          commands.add(command);
          continue;
        } else if (Objects.equals(input, "")) {
          continue;
        }
        System.out.print("Input \"" + input + "\" not found, enter again: ");
        continue readCommand;
      }

      if (commands.isEmpty() && acceptNoCommand) {
        return null;
      }

      return commands;
    }
  }

  public StepType step(Command... commands) {
    Stopwatch stopwatch = new Stopwatch(true);
    assert commands.length > 0 : "Must at least provide one command";
    Map<Boolean, List<Command>> split =
        Arrays.stream(commands)
            .collect(Collectors.partitioningBy(command -> command.commandEnum.handleByClient()));

    StepType stepType = stepServerSideCommand(split.getOrDefault(false, new ArrayList<>()));
    stepClientSideCommands(split.getOrDefault(true, new ArrayList<>()));
    if (stepType == null) {
      return StepType.Special;
    }
    return stepType;
  }

  private void stepClientSideCommands(List<Command> commands) {
    for (Command command : commands) {
      switch (command.commandEnum) {
        case COMMAND_EXTLIST:
          CommandEnum.prettyPrintCommands(gameMode);
          break;
        case COMMAND_REDRAW:
          System.out.println(gameState);
          break;
        case ADDITIONAL_SHOW_VERBOSE:
          System.out.print(gameState.verbose());
          break;
        case ADDITIONAL_SHOW_SEED:
          Seed seed = client.sendGetSeed();
          Loggers.SeedLogger.info(seed);
          break;
        case COMMAND_INVENTORY:
        case COMMAND_INVENTTYPE: // Should do something differently
          System.out.println(gameState.player.inventory);
          break;
        default:
          throw new IllegalArgumentException(
              "This client side command does not have logic to handle it");
      }
    }
  }

  private StepType stepServerSideCommand(List<Command> commands) {
    if (commands.isEmpty()) {
      return null;
    }
    int n = commands.size();
    assert n <= 256 : "No more than 256 commands can be chained at once";
    byte[] msg = new byte[2 + 2 * n];
    msg[0] = Encoder.EncoderBit.StepBit.value;
    msg[1] = (byte) n;
    for (int i = 0; i < n; i++) {
      Command command = commands.get(i);

      if (command.commandEnum == CommandEnum.ADDITIONAL_ASCII) {
        msg[i * 2 + 2] = 0;
        msg[i * 2 + 3] = (byte) command.stroke.charAt(0);
      } else {
        msg[i * 2 + 2] = 1;
        int commandIndex = command.commandEnum.getIndex(gameMode);
        if (commandIndex < 0) {
          Loggers.NetHackLogger.warn("Invalid command");
          return StepType.Invalid;
        }
        msg[i * 2 + 3] = (byte) commandIndex;
      }
    }

    StepState stepState = client.sendStepBytes(msg);
    updateGameState(stepState);
    return StepType.Valid;
  }

  private void updateGameState(StepState stepState) {
    if (stepState.done) {
      Loggers.NetHackLogger.info("Game run terminated, step indicated: done");
      gameState.done = true;
      return;
    }

    // Update turn before overriding stats
    if (gameState.stats != null) {
      stepState.stats.turn.updateTurn(gameState.stats.turn);
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
      stepState.player.lastPrayerTurn = gameState.player.lastPrayerTurn;
    }
    gameState.player = stepState.player;
    assert gameState.player.position.z == 0
        : "Before this line the levelNr is not known, assert is not already set";
    gameState.player.position.z = gameState.getLevelNr();
    gameState.done = stepState.done;
    gameState.info = stepState.info;
  }

  public enum StepType {
    Invalid,
    Valid,
    Special,
    Terminated;
  }
}
