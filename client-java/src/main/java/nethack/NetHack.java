package nethack;

import connection.SendCommandClient;
import nethack.utils.RenderUtils;
import nethack.object.Action;
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
		
		step(Action.MISC_MORE);
		render();
	}
	
	public void loop() throws IOException {
		int step = 0;
		
		while (!gameState.done) {
			Action action = waitCommand();
			logger.info("Step: " + step++ + " Action: " + action);
			step(action);
			render();
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
	
	public Action waitCommand() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Input a command: ");
		Action action = null;
		
		while (action == null) {
			String input = scanner.nextLine();
			action = Action.fromValue(input);
			System.out.print("Input \"" + input + "\" not found, enter again: ");
		}
		
		return action;
	}
	
	public void step(Action action) throws IOException {		
		StepState stepState = commander.sendCommand("Step", action.index, StepState.class);
		
		// Add to world
		while (stepState.stats.levelNumber >= gameState.world.size()) {
			gameState.world.add(null);
		}
		gameState.world.set(stepState.stats.levelNumber, stepState.level);
		
		// Set all members to the correct values
		this.gameState.message = stepState.message;
		this.gameState.player = stepState.player;
		this.gameState.stats = stepState.stats;
		this.gameState.done = stepState.done;
		this.gameState.info = stepState.info;
	}
}
