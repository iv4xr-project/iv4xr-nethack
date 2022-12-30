package nethack;

import java.io.IOException;
import java.io.Console;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

import connection.SendCommandClient;
import eu.iv4xr.framework.spatial.IntVec2D;
import nethack.utils.RenderUtils;
import nl.uu.cs.aplib.utils.Pair;

public class NetHack {	
	public Blstats stats;
	public boolean done;
	public Entity[][] entities;
	private SendCommandClient commander;
	
	public NetHack(SendCommandClient commander) {
		this.commander = commander;
//		Entity[][] entities, Blstats stats
//		step(entities, stats);
	}
	
	public void init() throws IOException {
		commander.readerwriter.read(Object.class);
		commander.sendCommand("Reset", "", GameState.class);
		
		step(Action.MISC_MORE);
		render();
	}
	
	public void loop() throws IOException {
		int step = 0;
		
		while (!done) {
			System.out.println("Step: " + step++);
			Action action = waitCommand();
			step(action);
			render();
		}
	}
	
	public void render() throws IOException {
		commander.writeCommand("Render", "");
		RenderUtils.render(entities);
	}
	
	public Action waitCommand() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Input a command: ");
		
		while (true) {
			String input = scanner.nextLine();
			
			Action action = Action.fromValue(input);
			if (action != null) {
				return action;
			}
			
			System.out.print("Input \"" + input + "\" not found, enter again: ");
		}
	}
	
	public void step(Action action) throws IOException {
		GameState gameState = commander.sendCommand("Step", action.index, GameState.class);
		GameInfo gameInfo = commander.readerwriter.read(GameInfo.class);

		this.entities = gameState.entities;
		this.stats = gameState.blstats;
		this.done = gameInfo.done;
	}
    
    public static class GameInfo
    {
    	public boolean done;
    	public Object info;
    }

    public static class GameState
    {
    	public Blstats blstats;
    	public Entity[][] entities;
    }
}
