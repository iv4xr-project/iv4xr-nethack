import java.io.IOException;

import connection.SendCommandClient;

/**
 * Hello world!
 *
 */
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

		Object init = commander.readerwriter.read(Object.class);
		System.out.println("Initial state: " + init);

		Object reset = commander.sendCommand("Reset", "", Object.class);
		System.out.println("Reset:" + reset);
		commander.writeCommand("Render", "");

		Object stepState = commander.sendCommand("Step", new DummyAction(), Object.class);
		Object information = commander.readerwriter.read(Object.class);
		commander.writeCommand("Render", "");

		commander.writeCommand("Close", "");
    	commander.close();
    }
}

class DummyAction {
	int Action = 1;
}
