package nethack;

import connection.ConnectionLoggers;
import connection.SocketClient;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import nethack.object.Seed;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {
  static final Logger logger = LogManager.getLogger(ConnectionLoggers.ConnectionLogger);

  public static void main(String[] args) throws IOException {
    // Initialize socket connection
    Pair<String, Integer> info = Config.getConnectionInfo();
    SocketClient client = new SocketClient(info.fst, info.snd);
    //    SocketClient client = new SocketClient("127.0.0.1", 5001);
    if (!client.socketReady()) {
      logger.fatal("Unsuccessful socket connection");
      return;
    }

    playGame(client);

    // Close socket connection
    logger.info("Closing connection");
    client.close();
  }

  private static void playGame(SocketClient client) throws IOException {
    NetHack nethack = new NetHack(client, Config.getSeed());
    nethack.loop();
    nethack.close();
  }

  private static void goThroughGame(SocketClient client) throws IOException {
    NetHack nethack = new NetHack(client, Config.getSeed());

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
