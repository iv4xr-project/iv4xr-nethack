package util;

import nethack.object.Seed;
import nethack.object.Turn;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.commons.configuration2.*;
import org.apache.commons.configuration2.convert.LegacyListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

public class Config {
  static PropertiesConfiguration config = new PropertiesConfiguration();

  static {
    try {
      config.setListDelimiterHandler(new LegacyListDelimiterHandler(','));
      final FileHandler handler = new FileHandler(config);
      handler.setFileName("src/main/resources/config.properties");
      handler.load();
    } catch (ConfigurationException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public static Pair<String, Integer> getConnectionInfo() {
    String ip = config.getString("IP", "127.0.0.1");
    int port = config.getInt("PORT", 5001);
    return new Pair<>(ip, port);
  }

  public static String getLogConfig() {
    return config.getString("LOG_CONFIG", "src/main/resources/log4j2.xml");
  }

  public static Seed getSeed() {
    String[] seed = config.getStringArray("SEED");
    if (seed.length == 0) {
      return Seed.randomSeed();
    }

    assert seed.length == 2
        : String.format("There must be 0 or 2 strings as seed, but was of length %d", seed.length);

    // Create seed from the two strings
    String core = seed[0];
    String disp = seed[1];
    return new Seed(core, disp, false);
  }

  public static String getCharacter() {
    return config.getString("CHARACTER", "mon-hum-neu-mal");
  }

  public static Turn getStartTurn() {
    // Autoplay overrides start turn
    if (getAutoPlay()) {
      return new Turn(1000000);
    }

    int[] startTurn = config.get(int[].class, "START_TURN", new int[0]);
    if (startTurn.length == 0) {
      return Turn.startTurn;
    }

    assert startTurn.length == 2
        : String.format(
            "There must be 0 or 2 ints as start turn, but was of length %d", startTurn.length);

    // Create turn from the two ints
    int time = startTurn[0];
    int step = startTurn[1];
    return new Turn(time, step);
  }

  public static boolean getAutoPlay() {
    return config.getBoolean("AUTO_PLAY", false);
  }

  public static boolean getSoundState() {
    return config.getBoolean("SOUND", true);
  }

  public static String getReplayFile() {
    return config.getString("REPLAY_FILE", "logs/replay.log");
  }

  public static boolean getCollectCoverage() {
    return config.getBoolean("COLLECT_COVERAGE", false);
  }

  public static boolean getGenerateHTML() {
    return config.getBoolean("GENERATE_HTML", false);
  }
}
