package connection.messagedecoder;

import connection.ObservationMessage;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import nethack.object.*;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.Logger;
import util.Loggers;
import util.Stopwatch;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class ObservationMessageDecoder extends Decoder {
  private static final Logger logger = Loggers.ProfilerLogger;

  public static ObservationMessage decode(DataInputStream input) {
    try {
      ObservationMessage observationMessage = new ObservationMessage();

      Stopwatch stopwatch = new Stopwatch();
      stopwatch.start();

      int inputByte = input.readByte();
      assert inputByte == DecoderBit.ObservationBit.value;
      logger.trace("READ BIT TOOK: %d", stopwatch.split());

      Pair<Stats, Player> pair = StatsDecoder.decode(input);
      observationMessage.stats = pair.fst;
      observationMessage.player = pair.snd;

      logger.trace("READ STATS TOOK: %d", stopwatch.split());

      byte[] chars = input.readNBytes(256);
      observationMessage.message = new String(chars, StandardCharsets.UTF_8);
      observationMessage.message = observationMessage.message.replaceAll("\0", "");

      long now_1 = System.nanoTime();
      logger.trace("READ MESSAGE TOOK: %d", stopwatch.split());

      long total = 0;
      byte[] entities = input.readNBytes(4 * Level.SIZE.width * Level.SIZE.height);
      for (int y = 0; y < Level.SIZE.height; y++) {
        int rowOffset = 4 * Level.SIZE.width * y;
        for (int x = 0; x < Level.SIZE.width; x++) {
          int offset = rowOffset + x * 4;
          char symbol = (char) entities[offset];
          int colorCode = entities[offset + 1];
          int glyph = (entities[offset + 2] << 8) + entities[offset + 3];
          observationMessage.entities[y][x] = EntityDecoder.decode(input, symbol, colorCode, glyph);
        }
      }
      logger.trace("READ MAP TOOK: %d", stopwatch.split());

      int nr_items = input.readByte();
      observationMessage.items = new Item[nr_items];

      for (int i = 0; i < nr_items; i++) {
        observationMessage.items[i] = ItemDecoder.decode(input);
      }
      logger.trace("READ ITEMS TOOK: %d", stopwatch.split());

      return observationMessage;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
