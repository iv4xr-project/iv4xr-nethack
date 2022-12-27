import java.io.IOException;
import java.util.Random;

import connection.SendCommandClient;
import nethack.Action;
import nethack.Blstats;

public class App
{
    public static void main( String[] args ) throws IOException
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
		while (true) {
			GameState gameState = commander.sendCommand("Step", action_index, GameState.class);
			ActionState actionState = commander.readerwriter.read(ActionState.class);
			commander.writeCommand("Render", "");
			int n = dice.nextInt(actionState.actions.length);
			String s = String.format("%d: %s: %d", n, actionState.actions[n], actionState.actions[n].value);
			
			action_index = n;
			
//			for (int i = 0; i < actionState.actions.length; i++) {
//				String s = String.format("%d: %s: %d", i, actionState.actions[i], actionState.actions[i].value);
//				System.out.println(s);
//			}
			
			if (actionState.done) {
				break;
			}
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
    	public int[][] glyphs;
    }
}
