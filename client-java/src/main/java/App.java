import connection.ConnectionLoggers;
import connection.SendCommandClient;
import nethack.NetHack;
import nethack.object.Seed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class App {
    static final Logger logger = LogManager.getLogger(ConnectionLoggers.ConnectionLogger);

    public static void main(String[] args) throws IOException {
        // Initialize socket connection
        SendCommandClient commander = new SendCommandClient("127.0.0.1", 5001);
        if (!commander.socketReady()) {
            logger.fatal("Unsuccessful socket connection");
            return;
        }

        // Main game loop
        NetHack nethack = new NetHack(commander);

        nethack.setSeed(Seed.presets[0]);
        Seed currentSeed = nethack.getSeed();
        System.out.println(currentSeed);

        nethack.loop();
        nethack.close();

        // Close socket connection
        logger.info("Closing connection");
        commander.close();
    }
}
