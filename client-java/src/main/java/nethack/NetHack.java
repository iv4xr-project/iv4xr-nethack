package nethack;

import connection.SendCommandClient;
import nethack.utils.RenderUtils;
import nethack.object.Command;
import nethack.object.Level;

import java.io.IOException;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetHack {
	public static final Logger logger = LogManager.getLogger(NetHack.class);
	private SendCommandClient commander;
	public GameState gameState = new GameState();

	public NetHack(SendCommandClient commander) {
		this.commander = commander;
	}

	public void init() throws IOException {
		logger.info("Initialize game");
		commander.read(Object.class);
		commander.sendCommand("Reset", "", Object.class);

		step(Command.MISC_MORE);
		render();
	}

	public void loop() throws IOException {
		int step = 0;

		while (!gameState.done) {
			Command command = waitCommand();
			if (command == Command.COMMAND_EXTLIST) {
				Command.prettyPrintActions();
			} else if (command == Command.COMMAND_REDRAW) {
				render();
			} else {
				logger.info("Step: " + step++ + " Action: " + command);
				step(command);
				render();
			}
		}
	}

	public void close() throws IOException {
		logger.info("Close game");
		commander.writeCommand("Close", "");
	}

	public Level level() {
		return gameState.level();
	}

	public void render() throws IOException {
		commander.writeCommand("Render", "");
		RenderUtils.render(gameState);
	}

	public Command waitCommand() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Input a command: ");

		while (true) {
			String input = scanner.nextLine();
			Command command = Command.fromValue(input);
			if (command != null) {
				return command;
			}
			System.out.print("Input \"" + input + "\" not found, enter again: ");
		}
	}

	public void step(Command command) throws IOException {
		StepState stepState = commander.sendCommand("Step", command.index, StepState.class);

		if (gameState.world.size() != 0) {
//			System.out.print("VisibleTiles=");
//			gameState.level().visibleTiles();
//			System.out.print("DifferentTiles=");
//			gameState.level().UpdateMap(stepState.level);
		}

		// Add to world
		while (stepState.stats.levelNumber > gameState.world.size()) {
			gameState.world.add(null);
		}
		gameState.world.set(stepState.stats.levelNumber - 1, stepState.level);

		// Set all members to the correct values
		gameState.message = stepState.message;
		gameState.player = stepState.player;
		gameState.stats = stepState.stats;
		gameState.done = stepState.done;
		gameState.info = stepState.info;
		
		
	}
}
