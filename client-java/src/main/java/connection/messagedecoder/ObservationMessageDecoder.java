package connection.messagedecoder;

import connection.ConnectionLoggers;
import connection.ObservationMessage;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import nethack.object.*;
import nl.uu.cs.aplib.utils.Pair;
import org.apache.logging.log4j.Logger;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class ObservationMessageDecoder extends Decoder {
  private static final Logger logger = ConnectionLoggers.ProfilerLogger;

  public static ObservationMessage decode(DataInputStream input) {
    try {
      ObservationMessage observationMessage = new ObservationMessage();
      boolean verbose = false;

      long now = System.nanoTime();

      int inputByte = input.readByte();
      assert inputByte == DecoderBit.ObservationBit.value;
      long now_bit = System.nanoTime();
      logger.trace(String.format("READ BIT TOOK: %d", now_bit - now));

      Pair<Stats, Player> pair = StatsDecoder.decode(input);
      observationMessage.stats = pair.fst;
      observationMessage.player = pair.snd;

      long now_blstats = System.nanoTime();
      logger.trace(String.format("READ BLSTATS TOOK: %d", now_blstats - now_bit));

      byte[] chars = input.readNBytes(256);
      observationMessage.message = new String(chars, StandardCharsets.UTF_8);
      observationMessage.message = observationMessage.message.replaceAll("\0", "");

      long now_1 = System.nanoTime();
      logger.trace(String.format("READ MESSAGE TOOK: %d", now_1 - now_blstats));

      long total = 0;
      for (int y = 0; y < Level.SIZE.height; y++) {
        byte[] entities = input.readNBytes(4 * Level.SIZE.width);
        for (int x = 0; x < Level.SIZE.width; x++) {
          long inbetween = System.nanoTime();
          char symbol = (char) entities[x * 4];
          int colorCode = entities[x * 4 + 1];
          int glyph = (entities[x * 4 + 2] << 8) + entities[x * 4 + 3];
          observationMessage.entities[y][x] = EntityDecoder.decode(input, symbol, colorCode, glyph);
          long time = System.nanoTime();
          total += time - inbetween;
        }
      }
      long now_2 = System.nanoTime();
      logger.trace(String.format("READ MAP TOOK: %d", now_2 - now_1));

      int nr_items = input.readByte();
      observationMessage.items = new Item[nr_items];

      for (int i = 0; i < nr_items; i++) {
        observationMessage.items[i] = ItemDecoder.decode(input);
      }
      long now_3 = System.nanoTime();
      logger.trace(String.format("READ ITEMS TOOK: %d", now_3 - now_2));

      return observationMessage;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
}
