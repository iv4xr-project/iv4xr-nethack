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
//		for (Color e: Color.values()) {
//            System.out.println("\033[" + e.colorCode + "m " + e.name());
//        }
//		
    	loop();
    }
	
    public static void loop() throws IOException
    {
    	SendCommandClient commander = new SendCommandClient("127.0.0.1", 5001);
    	commander.turnDebugMode(true);
		if (!commander.socketReady()) {
			System.out.println("Unsuccesful");
			return;
		} else {
			System.out.println("Succesful");
		}

		commander.readerwriter.read(Object.class);
		commander.sendCommand("Reset", "", GameState.class);
		int action_index = 0;
		
		Random dice = new Random();
		int step = 0;
		while (true) {
			GameState gameState = commander.sendCommand("Step", action_index, GameState.class);
			ActionState actionState = commander.readerwriter.read(ActionState.class);
			commander.writeCommand("Render", "");
			int n = dice.nextInt(actionState.actions.length);
			String s = String.format("%d: %s", n, actionState.actions[n]);
			
			action_index = n;
			RenderUtils.render(gameState.chars, gameState.colors);
			if (actionState.done) {
				break;
			}
			step++;
			System.out.println("Step " + step);
//			break;
		}

		commander.writeCommand("Close", "");
    	commander.close();
    }
    
    public static class ActionState
    {
    	public boolean done;
    	public Action[] actions;
    }

    public static class GameState
    {
    	public Blstats blstats;
    	public Entity[][] chars;
    	public Color[][] colors;
    }
}
