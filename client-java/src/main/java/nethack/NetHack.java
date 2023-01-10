package nethack;

import connection.SendCommandClient;
import nethack.object.Command;
import nethack.object.Level;

import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetHack {
	public static final Logger logger = LogManager.getLogger(NetHack.class);
	public GameState gameState = new GameState();
	private SendCommandClient commander;

	public NetHack(SendCommandClient commander) {
		this.commander = commander;
		logger.info("Initialize game");
		commander.read(Object.class);
		reset();
	}
	
	public void reset() {
		commander.sendCommand("Reset", "", Object.class);

		step(Command.MISC_MORE);
		render();
	}

	public void loop() {
		while (!gameState.done) {
			Command command = waitCommand(false);
			if (command == Command.COMMAND_EXTLIST) {
				Command.prettyPrintActions();
			} else if (command == Command.COMMAND_REDRAW) {
				render();
			} else {
				step(command);
				render();
			}
		}
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

	public void step(Command command) {
		logger.info("Command: " + command);
		StepState stepState = commander.sendCommand("Step", command.index, StepState.class);
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

	}
}
