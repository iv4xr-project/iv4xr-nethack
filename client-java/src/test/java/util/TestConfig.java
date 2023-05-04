package util;

import java.util.Iterator;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.convert.LegacyListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

public class TestConfig {
  public static void setConfigFile(String configFilename) {
    PropertiesConfiguration config = Config.config;
    PropertiesConfiguration overrideConfig = new PropertiesConfiguration();
    overrideConfig.setListDelimiterHandler(new LegacyListDelimiterHandler(','));
    final FileHandler handler = new FileHandler(overrideConfig);
    handler.setFileName("src/test/resources/" + configFilename);
    try {
      handler.load();
    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }

    for (Iterator<String> it = overrideConfig.getKeys(); it.hasNext(); ) {
      String key = it.next();
      config.setProperty(key, overrideConfig.getProperty(key));
    }
  }

  public static void main(String[] args) {
    setConfigFile("testConfig.properties");
    System.out.println(ConfigurationUtils.toString(Config.config));
  }
}
