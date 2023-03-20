package connection.messagedecoder;

import connection.ObservationMessage;
import java.io.DataInputStream;
import java.io.IOException;
import nethack.object.*;
import nl.uu.cs.aplib.utils.Pair;
import util.Loggers;
import util.Stopwatch;

// Source: https://studytrails.com/2016/09/12/java-google-json-type-adapter/
public class ObservationMessageDecoder extends Decoder {
  public static ObservationMessage decode(DataInputStream input) {
    try {
      ObservationMessage observationMessage = new ObservationMessage();
      Stopwatch stopwatch = new Stopwatch(true);

      int inputByte = input.readByte();
      assert inputByte == DecoderBit.ObservationBit.value;
      Loggers.ProfilerLogger.trace("READ BIT TOOK: %fs", stopwatch.split());

      Pair<Stats, Player> pair = StatsDecoder.decode(input);
      observationMessage.stats = pair.fst;
      observationMessage.player = pair.snd;
      Loggers.ProfilerLogger.trace("READ STATS TOOK: %fs", stopwatch.split());

      observationMessage.message = readString(input);
      Loggers.ProfilerLogger.trace("READ MESSAGE TOOK: %fs", stopwatch.split());

      long total = 0;
      int bytesPerEntry = 6;
      byte[] entities = input.readNBytes(bytesPerEntry * Level.SIZE.width * Level.SIZE.height);
      for (int y = 0; y < Level.SIZE.height; y++) {
        int rowOffset = bytesPerEntry * Level.SIZE.width * y;
        for (int x = 0; x < Level.SIZE.width; x++) {
          int offset = rowOffset + x * bytesPerEntry;
          char symbol = (char) entities[offset];
          int colorCode = entities[offset + 1];
          int glyph = (entities[offset + 2] << 8) + entities[offset + 3];
          int id = (entities[offset + 4] << 8) + entities[offset + 5];
          observationMessage.entities[y][x] =
              EntityDecoder.decode(input, symbol, colorCode, glyph, id);
        }
      }
      Loggers.ProfilerLogger.trace("READ MAP TOOK: %f", stopwatch.split());

      int nr_items = input.readByte();
      observationMessage.items = new Item[nr_items];

      for (int i = 0; i < nr_items; i++) {
        observationMessage.items[i] = ItemDecoder.decode(input);
      }
      Loggers.ProfilerLogger.trace("READ ITEMS TOOK: %f", stopwatch.split());

      return observationMessage;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
