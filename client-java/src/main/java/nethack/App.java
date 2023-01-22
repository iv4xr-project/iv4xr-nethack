package nethack;

import connection.ConnectionLoggers;
import connection.SendCommandClient;
import nethack.object.Seed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class App {
    static final Logger logger = LogManager.getLogger(ConnectionLoggers.ConnectionLogger);

    public static void main(String[] args) throws IOException {
        // Initialize socket connection
        SendCommandClient commander = new SendCommandClient("127.0.0.1", 5001);
        if (!commander.socketReady()) {
            logger.fatal("Unsuccessful socket connection");
            return;
        }

        playGame(commander);

        // Close socket connection
        logger.info("Closing connection");
        commander.close();
    }

    private static void playGame(SendCommandClient commander) throws IOException {
        NetHack nethack = new NetHack(commander);
        nethack.loop();
        nethack.close();
    }

    private static void gotThroughGame(SendCommandClient commander) throws IOException {
        NetHack nethack = new NetHack(commander, Seed.presets[1]);

        // Eternally go through new seeds
        while (true) {
            Seed seed = Seed.randomSeed();
            nethack.setSeed(seed);
            System.out.println(seed);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
