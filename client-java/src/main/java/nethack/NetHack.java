package nethack;

import connection.SendCommandClient;
import nethack.object.Command;
import nethack.object.GameMode;
import nethack.object.Level;
import nethack.object.Seed;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetHack {
	public static final Logger logger = LogManager.getLogger(NetHack.class);
	public GameState gameState = new GameState();
	public GameMode gameMode;
	public Seed seed;
	
	SendCommandClient commander;

	public NetHack(SendCommandClient commander) {
		init(commander, GameMode.NethackChallenge);
	}
	
	public NetHack(SendCommandClient commander, GameMode gameMode) {
		init(commander, gameMode);
	}
	
	private void init(SendCommandClient commander, GameMode gameMode) {
		this.commander = commander;
		this.gameMode = gameMode;
		logger.info("Initialize game");
		commander.read(Object.class);
		reset();
	}
	
	public void setSeed(Seed seed) {
		gameMode = GameMode.Nethack;
		commander.writeCommand("Set_seed", seed);
		reset();
	}
	
	public Seed getSeed() {
		return commander.sendCommand("Get_seed", null, Seed.class);
	}
	
	public void reset() {
		commander.sendCommand("Reset", gameMode.toString(), Object.class);

		step(Command.MISC_MORE);
		render();
	}

	public void loop() {
		while (!gameState.done) {
			Command command = waitCommand(false);
			if (command == Command.COMMAND_EXTLIST) {
				Command.prettyPrintActions(gameMode);
			} else if (command == Command.COMMAND_REDRAW) {
				render();
			} else if (step(command)) {
				render();
			}
		}
		logger.info("Gamestate indicates it is done, loop stopped");
	}

	public void close() {
		logger.info("Close game");
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
			if (command != null || acceptNoCommand) {
				return command;
			}
			System.out.print("Input \"" + input + "\" not found, enter again: ");
		}
	}

	public boolean step(Command command) {
		int index = command.getIndex(gameMode);
		if (index < 0) {
			logger.warn(String.format("Command: %s not available in GameMode: %s", command, gameMode));
			return false;
		}
		
		logger.info("Command: " + command);
		StepState stepState = commander.sendCommand("Step", index, StepState.class);
		stepState.level.setRemovedEntities(gameState.level());
		
		// Add to world
		if (stepState.stats.zeroIndexLevelNumber == gameState.world.size()) {
			gameState.world.add(null);
		}

		if (stepState.stats.zeroIndexLevelNumber >= 0) {
			gameState.world.set(stepState.stats.zeroIndexLevelNumber, stepState.level);
		}

		// Set all members to the correct values
		gameState.message = stepState.message;
		gameState.player = stepState.player;
		gameState.stats = stepState.stats;
		gameState.done = stepState.done;
		gameState.info = stepState.info;
		return true;
	}
}
