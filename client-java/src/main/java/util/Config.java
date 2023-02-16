package util;

import java.util.ArrayList;
import java.util.List;
import nethack.object.Seed;
import nethack.object.Turn;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

public class Config {
  static final CompositeConfiguration config;

  static {
    config = new CompositeConfiguration();
    config.addConfiguration(new SystemConfiguration());
    try {
      config.addConfiguration(new PropertiesConfiguration("config.properties"));
    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  public static Pair<String, Integer> getConnectionInfo() {
    String ip = config.getString("IP", "127.0.0.1");
    int port = config.getInt("PORT", 5001);
    return new Pair<>(ip, port);
  }

  public static String getLogConfig() {
    return config.getString("LOG_CONFIG", "log4j2.xml");
  }

  public static Seed getSeed() {
    List<Object> seed = config.getList("SEED", new ArrayList<Object>());
    assert seed.isEmpty() || seed.size() == 2
        : String.format("There must be 0 or 2 strings as seed, but was of length %d", seed.size());
    if (seed.isEmpty()) {
      return Seed.randomSeed();
    }

    // Create seed from the two strings
    String core = seed.get(0).toString();
    String disp = seed.get(1).toString();
    return new Seed(core, disp, false);
  }

  public static Turn getStartTurn() {
    List<Object> startTurn = config.getList("START_TURN", new ArrayList<Object>());
    assert startTurn.isEmpty() || startTurn.size() == 2
        : String.format(
            "There must be 0 or 2 ints as start turn, but was of length %d", startTurn.size());

    if (startTurn.isEmpty()) {
      return Turn.startTurn;
    }

    // Create turn from the two ints
    int time = Integer.parseInt(startTurn.get(0).toString());
    int step = Integer.parseInt(startTurn.get(1).toString());
    Turn turn = new Turn(time);
    turn.step = step;
    return turn;
  }

  public static boolean getSoundState() {
    return config.getBoolean("SOUND", true);
  }
}
