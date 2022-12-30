import java.io.IOException;
import java.util.Random;

import connection.SendCommandClient;
import nethack.Action;
import nethack.Blstats;
import nethack.Entity;
import nethack.NetHack;
import nethack.utils.RenderUtils;
import java.util.Scanner;

public class App
{
	public static void main( String[] args ) throws IOException
    {
		SendCommandClient commander = init_connection();
		if (commander == null) {
			System.out.println("Unsuccesful socket connection");
			return;
		}
		
		NetHack app = new NetHack(commander);
		app.init();
		app.loop();
		
		close_connection(commander);
    }
	
	public static SendCommandClient init_connection() throws IOException
	{
    	SendCommandClient commander = new SendCommandClient("127.0.0.1", 5001);
    	commander.turnDebugMode(false);
		if (!commander.socketReady()) {
			return null;
		}
		
		return commander;
	}
	
	public static void close_connection(SendCommandClient commander) throws IOException
	{
		System.out.println("Closing connection");
		commander.writeCommand("Close", "");
    	commander.close();
	}
}
