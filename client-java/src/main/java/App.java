import java.io.IOException;
import java.util.Random;

import connection.SendCommandClient;
import nethack.Action;
import nethack.Blstats;
import nethack.Color;
import nethack.Entity;
import nethack.utils.RenderUtils;

public class App
{
	public static void main( String[] args ) throws IOException
    {
		SendCommandClient commander = init_connection();
		if (commander == null) {
			System.out.println("Unsuccesful socket connection");
			return;
		}
		
    	loop(commander);
    	close_connection(commander);
    }
	
	public static SendCommandClient init_connection() throws IOException
	{
    	SendCommandClient commander = new SendCommandClient("127.0.0.1", 5001);
    	commander.turnDebugMode(false);
		if (!commander.socketReady()) {
			return null;
		}

		commander.readerwriter.read(Object.class);
		commander.sendCommand("Reset", "", GameState.class);
		
		return commander;
	}
	
	public static void close_connection(SendCommandClient commander) throws IOException
	{
		System.out.println("Closing connection");
		commander.writeCommand("Close", "");
    	commander.close();
	}
	
    public static void loop(SendCommandClient commander) throws IOException
    {		
		Random dice = new Random();
		int step = 0;
		boolean done = false;
		
		while (!done) {
			System.out.println("Step: " + step++);
			int random_action = dice.nextInt(Action.values().length);
			GameState gameState = commander.sendCommand("Step", random_action, GameState.class);
			GameInfo actionState = commander.readerwriter.read(GameInfo.class);
			
			commander.writeCommand("Render", "");
			RenderUtils.render(gameState.entities);
			done = actionState.done;
			System.in.read();
		}
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
