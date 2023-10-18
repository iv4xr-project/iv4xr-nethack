package nethack;

import static nethack.enums.CommandEnum.*;

import agent.navigation.hpastar.smoother.Direction;
import connection.SocketClient;
import connection.messageencoder.Encoder;
import java.util.*;
import java.util.stream.Collectors;
import nethack.enums.CommandEnum;
import nethack.enums.GameMode;
import nethack.object.*;
import nethack.object.items.Item;
import nethack.world.Level;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.commons.lang3.SerializationUtils;
import util.Config;
import util.CustomVec2D;
import util.Loggers;
import util.Replay;

public class NetHack {
  public GameState previousGameState = null;
  public final GameState gameState = new GameState();
  public GameMode gameMode = GameMode.NetHack;
  public Seed seed;
  public String character;
  SocketClient client;
  public List<LTLState> stateTrace = new ArrayList<>();

  public NetHack(SocketClient client, String character, Seed seed) {
    Loggers.NetHackLogger.info("Initialize game");
    this.client = client;
    this.character = character;
    setSeed(seed);
  }

  public NetHack(SocketClient client, Replay replay) {
    Loggers.NetHackLogger.info("Initialize game with replay");
    this.client = client;
    this.character = replay.character;
    setSeed(replay.seed);
    replay(replay);
  }

  public Seed getSeed() {
    Seed seed = client.sendGetSeed();
    Loggers.SeedLogger.info("Current seed: " + seed);
    return seed;
  }

  public void setSeed(Seed seed) {
    assert seed != null : "Must create game with a seed";
    System.out.printf("Init game with seed: %s%n", seed);
    this.seed = seed;
    reset();
  }

  public void reset() {
    Loggers.SeedLogger.info("New seed is:" + seed);
    Loggers.ReplayLogger.info("SEED=%s", seed.shortString());
    Loggers.ReplayLogger.info("CHARACTER=%s", character);
    client.sendReset(gameMode.toString(), character, seed);
    StepState stepState = client.readStepState();
    updateGameState(stepState);
    render();
  }

  public void loop() {
    while (!gameState.done) {
      List<Command> commands = waitCommands(false);
      StepType stepType = step(commands);
      if (stepType == StepType.Terminated) {
        break;
      } else if (stepType == StepType.Valid) {
        render();
      }
    }
    Loggers.NetHackLogger.info("GameState indicates it is done, loop stopped");
  }

  public void replay(Replay replay) {
    Turn startTurn = Config.getStartTurn();
    for (Pair<Turn, List<Command>> action : replay.actions) {
      if (gameState.stats.turn.compareTo(startTurn) >= 0) {
        break;
      }

      assert gameState.stats.turn.equals(action.fst)
          : String.format(
              "Turn was different. Expected %s but found %s", action.fst, gameState.stats.turn);
      step(action.snd);
    }

    if (!gameState.stats.turn.equals(Turn.startTurn)) {
      render();
    }
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

  public StepType step(List<Command> commands) {
    assert !commands.isEmpty() : "Must at least provide one command";
    Map<Boolean, List<Command>> split =
        commands.stream()
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
        case COMMAND_EXTLIST -> CommandEnum.prettyPrintCommands(gameMode);
        case COMMAND_REDRAW -> System.out.println(gameState);
        case ADDITIONAL_SHOW_VERBOSE -> System.out.print(gameState.verbose());
        case ADDITIONAL_SHOW_SEED -> {
          Seed seed = client.sendGetSeed();
          Loggers.SeedLogger.info(seed);
        }
        case COMMAND_INVENTORY, COMMAND_INVENTTYPE -> // Should do something differently
        System.out.println(gameState.player.inventory);
        default -> throw new IllegalArgumentException(
            "This client side command does not have logic to handle it");
      }
    }
  }

  private StepType stepServerSideCommand(List<Command> commands) {
    if (commands.isEmpty()) {
      return null;
    }
    Loggers.ReplayLogger.info("%s=%s", gameState.stats.turn, commands);
    Loggers.NetHackLogger.info(commands);
    int n = commands.size();
    assert n <= 256 : "No more than 256 commands can be chained at once";
    byte[] msg = new byte[2 + 2 * n];
    msg[0] = Encoder.EncoderBit.StepBit.value;
    msg[1] = (byte) n;
    for (int i = 0; i < n; i++) {
      Command command = commands.get(i);
      assert command != null : "Command cannot be null";

      // Quaff and read doesn't work in NLE, use direct command instead
      if (command.commandEnum == COMMAND_QUAFF) {
        command = Command.fromStroke("-q");
      } else if (command.commandEnum == COMMAND_READ) {
        command = Command.fromStroke("-r");
      } else if (command.commandEnum == COMMAND_WIELD) {
        command = Command.fromStroke("-w");
      } else if (command.commandEnum == COMMAND_DROP) {
        command = Command.fromStroke("-d");
      } else if (command.commandEnum == COMMAND_THROW) {
        command = Command.fromStroke("-t");
      } else if (command.commandEnum == COMMAND_APPLY) {
        command = Command.fromStroke("-a");
      }

      assert command != null : "Command cannot be null";
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

    // Keep copy of previous state, serialization is the chosen solution
    previousGameState = SerializationUtils.clone(gameState);

    // Update state with the result received from the socket
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
    if (stepState.message.contains("You attack thin air.")) {
      Loggers.NetHackLogger.warn("Is there a monster not updated?");
    }

    gameState.message = stepState.message;
    // Remember last position of player before setting
    if (gameState.player != null) {
      stepState.player.previousLocation = gameState.player.location;
      stepState.player.lastPrayerTurn = gameState.player.lastPrayerTurn;
    }

    Dlvl dlvl = stepState.stats.dlvl;
    CustomVec2D playerPos = stepState.player.location.pos;
    int lvlNr;
    if (gameState.dungeon.levelExists(dlvl)) {
      lvlNr = gameState.dungeon.getLevelNr(dlvl);
    } else {
      lvlNr = gameState.dungeon.levels.size();
    }

    // Set level numbers for tiles, monsters, and entities
    for (int y = 0; y < Level.SIZE.height; y++) {
      for (int x = 0; x < Level.SIZE.width; x++) {
        if (stepState.tiles[y][x] != null) {
          stepState.tiles[y][x].loc.lvl = lvlNr;
        }
      }
    }
    for (Entity e : stepState.entities) {
      e.loc.lvl = lvlNr;
    }
    for (Monster m : stepState.monsters) {
      m.loc.lvl = lvlNr;
    }

    Level level;
    if (!gameState.dungeon.levelExists(dlvl)) {
      level = new Level(stepState.symbols, stepState.tiles, stepState.monsters, stepState.entities);
      gameState.dungeon.newLevel(level, dlvl, stepState.player);
    } else {
      gameState.dungeon.getLevelNr(dlvl);
      level = gameState.dungeon.getLevel(dlvl);
      level.updateLevel(stepState.symbols, stepState.tiles, stepState.monsters, stepState.entities);

      // Shop door might be around
      if (gameState.message.matches(
          ".*You read: \"[C?][l?][o?][s?][e?][d?] [f?][o?][r?]"
              + " [i?][n?][v?][e?][n?][t?][o?][r?][y?]\"\\..*")) {
        level.markShopDoors(stepState.player.location.pos);
      }
    }

    gameState.player = stepState.player;
    assert gameState.player.location.lvl == 0
        : "Before this line the levelNr is not known, assert is not already set";
    gameState.player.location.lvl = gameState.getLevelNr();
    gameState.done = stepState.done;
    gameState.info = stepState.info;
  }

  public StepType apply(Item item) {
    return step(List.of(new Command(COMMAND_APPLY), new Command(item.symbol)));
  }

  public StepType apply(Item item, Direction direction) {
    return step(
        List.of(
            new Command(COMMAND_APPLY), new Command(item.symbol), Direction.getCommand(direction)));
  }

  public StepType move(Direction direction) {
    return step(List.of(Direction.getCommand(direction)));
  }

  public StepType kick(Direction direction) {
    return step(List.of(new Command(COMMAND_KICK), Direction.getCommand(direction)));
  }

  public StepType open(Direction direction) {
    return step(List.of(new Command(COMMAND_OPEN), Direction.getCommand(direction)));
  }

  public StepType fight(Direction direction) {
    return step(List.of(new Command(COMMAND_FIGHT), Direction.getCommand(direction)));
  }

  public StepType fire(Direction direction) {
    return step(List.of(new Command(COMMAND_FIRE), Direction.getCommand(direction)));
  }

  public StepType throwDagger(Item item, Direction direction) {
    return step(
        List.of(
            new Command(COMMAND_THROW), new Command(item.symbol), Direction.getCommand(direction)));
  }

  public StepType eat(Item item) {
    return step(
        List.of(new Command(COMMAND_EAT), new Command(item.symbol), new Command(MISC_MORE)));
  }

  public StepType search() {
    return step(List.of(new Command(COMMAND_SEARCH)));
  }

  public StepType quaff(Item item) {
    return step(
        List.of(new Command(COMMAND_QUAFF), new Command(item.symbol), new Command(MISC_MORE)));
  }

  public StepType wield(Item item) {
    return step(List.of(new Command(COMMAND_WIELD), new Command(item.symbol)));
  }

  public StepType pray() {
    gameState.player.lastPrayerTurn = gameState.stats.turn.time;
    return step(List.of(new Command(COMMAND_PRAY), new Command('y'), new Command(MISC_MORE)));
  }

  public enum StepType {
    Invalid,
    Valid,
    Special,
    Terminated
  }
}
