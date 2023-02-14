package util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class Loggers {
  static {
    // Set config file
    System.setProperty("log4j2.configurationFile", "log4j2.xml");
  }

  // Connection loggers
  public static final Logger ConnectionLogger = createLogger("\uD83D\uDCE1"); // 📡
  public static final Logger EncoderLogger = createLogger("\uD83D\uDCE6"); // 📦
  public static final Logger DecoderLogger = createLogger("\uD83D\uDCE8"); // 📬;

  // NetHack loggers
  public static final Logger NetHackLogger = createLogger("⚔️"); // ⚔️
  public static final Logger ProfilerLogger = createLogger("Profiler");
  public static final Logger SeedLogger = createLogger("\uD83C\uDF31"); // 🌱

  // Agent Loggers
  public static final Logger AgentLogger = createLogger("\uD83D\uDD75️"); // 🕵️
  public static final Logger WOMLogger = createLogger("\uD83C\uDF0E"); // 🌎
  public static final Logger NavLogger = createLogger("\uD83E\uDDED"); // 🧭
  public static final Logger GoalLogger = createLogger("\uD83D\uDCA1"); // 💡
  public static final Logger SoundLogger = createLogger("\uD83C\uDFB5"); // 🎵
  public static final Logger HPALogger = createLogger("HPA");

  public static Logger createLogger(String name) {
    return LogManager.getFormatterLogger(name);
  }

  // Test out logger colors
  public static void main(String[] args) throws Exception {
    // Set level to ALL beforehand
    // Source:
    // https://stackoverflow.com/questions/23434252/programmatically-change-log-level-in-log4j2
    Logger exampleLogger = LogManager.getFormatterLogger("ExampleName");
    Configurator.setLevel(exampleLogger.getName(), Level.ALL);
    exampleLogger.trace("trace msg");
    exampleLogger.debug("debug msg");
    exampleLogger.info("info msg");
    exampleLogger.warn("warn msg");
    exampleLogger.error("error msg");
    exampleLogger.fatal("fatal msg");
  }
}
