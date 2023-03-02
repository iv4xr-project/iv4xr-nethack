package util;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;

public class TestConfig {
  public static void setConfigFile(String configFilename) {
    Config.config = new CompositeConfiguration();
    Config.config.addConfiguration(new SystemConfiguration());
    try {
      Config.config.addConfiguration(new PropertiesConfiguration(configFilename));
    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }
}
