package nethack;

import java.util.ArrayList;
import java.util.List;
import nethack.object.Seed;
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

  public static Seed getSeed() {
    List<Object> seed = config.getList("SEED", new ArrayList<Object>());
    assert seed.size() == 2 || seed.isEmpty()
        : String.format("There must be 0 or 2 strings as seed, but was of length %d", seed.size());
    if (seed.isEmpty()) {
      return Seed.randomSeed();
    }

    // Create seed from the two strings
    String core = (String) seed.get(0);
    String disp = (String) seed.get(1);
    return new Seed(core, disp, false);
  }

  public static int getStartTurn() {
    return config.getInt("START_TURN", 1);
  }

  public static boolean getSoundState() {
    return config.getBoolean("SOUND", true);
  }
}
