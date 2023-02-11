package nethack;

import connection.SocketClient;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import nethack.object.Seed;
import org.apache.logging.log4j.Logger;

public class App {
  static final Logger seedLogger = NetHackLoggers.SeedLogger;

  public static void main(String[] args) throws IOException {
    // Initialize socket connection
    SocketClient client = new SocketClient();
    playGame(client);
    client.close();
  }

  private static void playGame(SocketClient client) {
    NetHack nethack = new NetHack(client, Config.getSeed());
    nethack.loop();
    nethack.close();
  }

  private static void goThroughGame(SocketClient client) {
    NetHack nethack = new NetHack(client, Config.getSeed());

    // Eternally go through new seeds
    while (true) {
      Seed seed = Seed.randomSeed();
      nethack.setSeed(seed);
      seedLogger.info(seed);
      try {
        TimeUnit.SECONDS.sleep(1);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
