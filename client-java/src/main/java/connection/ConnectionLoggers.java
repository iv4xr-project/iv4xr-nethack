package connection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectionLoggers {
  public static final Logger ConnectionLogger = LogManager.getLogger("\uD83D\uDCE1"); // 📡
  public static final Logger EncoderLogger = LogManager.getLogger("\uD83D\uDCE6"); // 📦
  public static final Logger DecoderLogger = LogManager.getLogger("\uD83D\uDCE8"); // 📬;
  public static final Logger ProfilerLogger = LogManager.getLogger("Profiler");
}
